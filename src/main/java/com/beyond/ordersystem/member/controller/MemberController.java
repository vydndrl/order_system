package com.beyond.ordersystem.member.controller;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.CommonResDto;
import com.beyond.ordersystem.member.dto.MemberSaveReqDto;
import com.beyond.ordersystem.member.dto.MemberResDto;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Autowired
    public MemberController(MemberService memberService, MemberRepository memberRepository) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
    }

    @PostMapping("/member/create")
    public ResponseEntity<Object> memberCreate (@Valid @RequestBody MemberSaveReqDto dto) {
        Member member = memberService.memberCreate(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "member is successfully created", member.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @GetMapping("/member/list")
    public ResponseEntity<Object> memberList(Pageable pageable) {
        Page<MemberResDto> dtos = memberService.memberList(pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "members are found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
