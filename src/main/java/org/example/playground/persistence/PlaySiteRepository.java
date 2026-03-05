package org.example.playground.persistence;

import org.example.playground.model.PlaySite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaySiteRepository extends JpaRepository<PlaySite, Long> {

}
