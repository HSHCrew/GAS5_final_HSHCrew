package org.zerock.Altari.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.Altari.dto.FamilyGroupDTO;
import org.zerock.Altari.entity.UserEntity;
import org.zerock.Altari.security.util.JWTUtil;
import org.zerock.Altari.service.FamilyService;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/altari/family")
@Log4j2
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;
    private final JWTUtil jwtUtil;

    @GetMapping("/groups")
    public ResponseEntity<List<FamilyGroupDTO>> getFamilyGroups(@RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {
        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        List<FamilyGroupDTO> familyGroups = familyService.readFamilyGroup(user);
        return ResponseEntity.ok(familyGroups);
    }

    @PostMapping("/group")
    public ResponseEntity<FamilyGroupDTO> createFamilyGroup(@RequestHeader("Authorization") String accessToken,
                                                            @RequestBody FamilyGroupDTO familyGroupDTO) throws UnsupportedEncodingException {
        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        FamilyGroupDTO createdGroup = familyService.createFamily(familyGroupDTO, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @PutMapping("/group/{familyGroupId}")
    public ResponseEntity<FamilyGroupDTO> updateFamilyGroup(@PathVariable Integer familyGroupId,
                                                            @RequestHeader("Authorization") String accessToken,
                                                            @RequestBody FamilyGroupDTO familyGroupDTO) throws UnsupportedEncodingException {
        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        FamilyGroupDTO updatedGroup = familyService.updateFamilyGroup(familyGroupId, familyGroupDTO, user);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/group/{familyGroupId}")
    public ResponseEntity<String> deleteFamilyGroup(@PathVariable Integer familyGroupId,
                                                    @RequestHeader("Authorization") String accessToken) throws UnsupportedEncodingException {
        UserEntity user = jwtUtil.getUserFromToken(accessToken);
        familyService.deleteFamilyGroup(familyGroupId, user);
        return ResponseEntity.ok("가족 그룹이 성공적으로 삭제되었습니다.");
    }
}
