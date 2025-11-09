package com.travelplanner.controller;

import com.travelplanner.dto.RegisterRequest;
import com.travelplanner.entity.User;
import com.travelplanner.security.CustomUserDetailsService;
import com.travelplanner.service.UserService;
import com.travelplanner.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

//    @Autowired
//    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {

        System.out.println("登录" + loginRequest.get("username"));
        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            loginRequest.get("username"),
//                            loginRequest.get("password")
//                    )
//            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.get("username"));
            final String jwt = jwtUtil.generateToken(userDetails);

            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            response.put("username", userDetails.getUsername());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("注册请求: " + registerRequest);

            // 验证必要字段
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("用户名不能为空");
            }
            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("密码不能为空");
            }
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("邮箱不能为空");
            }

            // 创建 User 实体
            User user = new User();
            user.setUsername(registerRequest.getUsername().trim());
            user.setPassword(registerRequest.getPassword().trim()); // 会在 service 中加密
            user.setEmail(registerRequest.getEmail().trim());

            // 手机号可选
            if (registerRequest.getPhone() != null && !registerRequest.getPhone().trim().isEmpty()) {
                user.setPhone(registerRequest.getPhone().trim());
            }

            // 注册用户
            User registeredUser = userService.registerUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户注册成功");
            response.put("username", registeredUser.getUsername());
            response.put("email", registeredUser.getEmail());

            System.out.println("注册成功: " + registeredUser.getUsername());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.out.println("注册失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("注册异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("注册失败: " + e.getMessage());
        }
    }
}