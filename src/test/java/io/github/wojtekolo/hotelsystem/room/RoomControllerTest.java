package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.room.dtos.RoomListItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;
    @Test
    void should_return_page_of_rooms() throws Exception {
        RoomListItem item = new RoomListItem(
                null,
                "101",
                BigDecimal.valueOf(500),
                3,
                "type",
                "status",
                1
        );
        given(roomService.getRooms(any())).willReturn(new SliceImpl<>(List.of(item)));

        mockMvc.perform(get("/rooms/roomlist")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("101"))
                .andExpect(jsonPath("$.content[0].pricePerNight").value(BigDecimal.valueOf(500)));

    }

    @Test
    void should_return_empty_slice_when_no_rooms() throws Exception {
        given(roomService.getRooms(any())).willReturn(new SliceImpl<>(List.of()));

        mockMvc.perform(get("/rooms/roomlist")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

    }
}