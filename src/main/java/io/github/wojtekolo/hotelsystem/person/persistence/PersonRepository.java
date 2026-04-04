package io.github.wojtekolo.hotelsystem.person.persistence;

import io.github.wojtekolo.hotelsystem.person.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}
