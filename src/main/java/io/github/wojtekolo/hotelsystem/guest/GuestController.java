package io.github.wojtekolo.hotelsystem.guest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/guest")
public class GuestController {
    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping("/add")
    public ResponseEntity<GuestDetails> addGuest(@RequestBody @Valid GuestCreateRequest createRequest){
        System.out.println(createRequest.person());
        System.out.println(createRequest.description());
        return ResponseEntity.ok(guestService.addGuest(createRequest));
    }
}
