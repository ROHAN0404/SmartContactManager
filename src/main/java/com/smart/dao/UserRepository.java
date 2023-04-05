package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smart.entities.User;

//interface for executing query
public interface UserRepository extends JpaRepository<User,Integer>{
	
	@Query(value="select * from User where user.email = :email",nativeQuery = true)
	public User getUserByUserName(@Param("email") String email);
}
