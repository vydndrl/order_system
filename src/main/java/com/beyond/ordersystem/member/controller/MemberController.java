package com.beyond.ordersystem.member.controller;

import com.beyond.ordersystem.common.auth.JwtToKenProvider;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.*;
import com.beyond.ordersystem.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@RestController
public class MemberController {

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private final MemberService memberService;
    private final JwtToKenProvider jwtToKenProvider;
    @Qualifier("2")
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public MemberController(MemberService memberService, JwtToKenProvider jwtToKenProvider, @Qualifier("2")RedisTemplate<String, Object> redisTemplate) {
        this.memberService = memberService;
        this.jwtToKenProvider = jwtToKenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/member/create")
    public ResponseEntity<Object> memberCreate (@Valid @RequestBody MemberSaveReqDto dto) {
        Member member = memberService.memberCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "member is successfully created", member.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    //    admin만 회원 목록 전체 조회 가능
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member/list")
    public ResponseEntity<Object> memberList(Pageable pageable) {
        Page<MemberResDto> dtos = memberService.memberList(pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "members are found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
    
//    본인은 본인 회원 정보만 조회 가능
    @GetMapping("/member/myinfo")
    public ResponseEntity<Object> memberMyInfo() {
        MemberResDto memberResDto = memberService.memberMyInfo();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "My Info", memberResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
    
    @PostMapping("/doLogin")
    public ResponseEntity<Object> doLogin(@RequestBody MemberLoginDto dto) {
//        email, password 가 일치하는지 검증
        Member member = memberService.login(dto);
//        일치할 경우 accessToken 생성
        String jwtToken = jwtToKenProvider.createToken(member.getEmail(), member.getRole().toString());
        String refreshToken = jwtToKenProvider.createRefreshToken(member.getEmail(), member.getRole().toString());

//        redis에 email과 rt를 key:value로 하여 저장
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS); // 240시간
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "success", loginInfo);
//        생성 된 토큰을 CommonResDto 에 담아 사용자에게 return
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PostMapping("/refresh-token")

    public ResponseEntity<?> generateNewAccesstoken(@RequestBody MemberRefreshDto dto) {
        String rt = dto.getRefreshToken();
        Claims claims = null;
        try {
//            코드를 통해 rt 검증
            claims = Jwts.parser().setSigningKey(secretKeyRt).parseClaimsJws(rt).getBody();
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED.value(), "invalid refresh token"), HttpStatus.UNAUTHORIZED);
        }
        String email = claims.getSubject();
        String role = claims.get("role").toString();

//        redis를 조회하여 rt 추가 검증
        Object obj = redisTemplate.opsForValue().get(email);
        if(obj == null || !obj.toString().equals(rt)) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED.value(), "invalid refresh token"), HttpStatus.UNAUTHORIZED);
        }


        String newAt = jwtToKenProvider.createToken(email, role);
        Map<String, Object> info = new HashMap<>();
        info.put("token", newAt);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "success", info);
//        생성 된 토큰을 CommonResDto 에 담아 사용자에게 return
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
