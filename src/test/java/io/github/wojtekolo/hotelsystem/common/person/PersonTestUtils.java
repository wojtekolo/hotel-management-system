package io.github.wojtekolo.hotelsystem.common.person;

import java.time.LocalDate;

public class PersonTestUtils {
    public static Person.PersonBuilder aValidPerson(){
        return Person.builder()
                .name("testName")
                .surname("testSurname")
                .birthDate(LocalDate.of(2000,10,10));
    }
}
