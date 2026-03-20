package com.practise.revision.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practise.revision.dto.OrderDto;
import com.practise.revision.dto.UserDto;
import com.practise.revision.dto.UserEvent;
import com.practise.revision.producer.UserEventProducer;
import com.practise.revision.service.OrderService;
import com.practise.revision.service.UserManagementService;
import com.practise.revision.service.serviceImpl.UserProfileImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementService userManagementService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserEventProducer userEventProducer;

    @MockBean
    private UserProfileImageService userProfileImageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_Success() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john@example.com");

        UserDto createdUser = new UserDto();
        createdUser.setId(1L);
        createdUser.setName("John Doe");
        createdUser.setEmail("john@example.com");

        when(userManagementService.createUser(any(UserDto.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(userManagementService).createUser(any(UserDto.class));
    }

    @Test
    void getUser_Success() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");

        when(userManagementService.getUser(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(userManagementService).getUser(1L);
    }

    @Test
    void updateUser_Success() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Updated Name");

        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setName("Updated Name");

        when(userManagementService.updateUser(any(UserDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/users/updateUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(userManagementService).updateUser(any(UserDto.class));
    }

    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(userManagementService).deleteUser(1L);

        mockMvc.perform(delete("/users/deleteUser/1"))
                .andExpect(status().isNoContent());

        verify(userManagementService).deleteUser(1L);
    }

    @Test
    void publishUserEvent_Success() throws Exception {
        UserEvent event = new UserEvent();
        event.setName("John Doe");

        doNothing().when(userEventProducer).sendUserCreatedEvent(any(UserEvent.class));

        mockMvc.perform(post("/users/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event published successfully"))
                .andExpect(jsonPath("$.user").value("John Doe"));

        verify(userEventProducer).sendUserCreatedEvent(any(UserEvent.class));
    }

    @Test
    void getUserOrders_Success() throws Exception {
        OrderDto order1 = new OrderDto();
        order1.setOrderId(1L);
        OrderDto order2 = new OrderDto();
        order2.setOrderId(2L);
        List<OrderDto> orders = Arrays.asList(order1, order2);

        when(orderService.getUserOrders(1L)).thenReturn(orders);

        mockMvc.perform(get("/users/1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[1].orderId").value(2));

        verify(orderService).getUserOrders(1L);
    }
}
