package io.github.wojtekolo.hotelsystem.guest;

import io.github.wojtekolo.hotelsystem.common.person.PersonRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class GuestService {
    private final GuestRepository guestRepository;
    private final PersonRepository personRepository;
    private final GuestMapper guestMapper;

    public GuestService(GuestRepository guestRepository, PersonRepository personRepository, GuestMapper guestMapper){
        this.guestRepository = guestRepository;
        this.personRepository = personRepository;
        this.guestMapper = guestMapper;
    }

    @Transactional
    public GuestDetails addGuest(GuestCreateRequest createRequest){
        Guest guest = guestMapper.toEntity(createRequest);
        Guest savedGuest = guestRepository.save(guest);
        return guestMapper.toDetails(savedGuest);
    }
}
