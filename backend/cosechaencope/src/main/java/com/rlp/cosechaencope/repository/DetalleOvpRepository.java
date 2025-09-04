package com.rlp.cosechaencope.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rlp.cosechaencope.model.DetalleOvp;

@Repository
public interface DetalleOvpRepository extends JpaRepository<DetalleOvp, Long> {


}
