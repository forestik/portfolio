package com.forestik.repo;

import com.forestik.entity.ContractAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractAddressesRepo extends JpaRepository<ContractAddress, Long> {

     List<ContractAddress> findAllByChain(@Param("chain") String chain);

//     Optional<Principal> findByFirstNameOrLastName(@Param("firstName")String firstName, @Param("lastName")String lastName);

}
