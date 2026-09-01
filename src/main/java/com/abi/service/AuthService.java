package com.abi.service;

import com.abi.request.LoginRequest;
import com.abi.request.RegisterRequest;
import com.abi.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse register(RegisterRequest registerRequest);
}
