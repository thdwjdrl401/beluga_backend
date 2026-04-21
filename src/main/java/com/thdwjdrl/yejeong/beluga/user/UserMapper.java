package com.thdwjdrl.yejeong.beluga.user;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

	@Select("""
			SELECT
			    user_id,
			    email,
			    created_at
			FROM users
			WHERE email = #{email}
			""")
	User findByEmail(@Param("email") String email);

	@Insert("""
			INSERT INTO users (
			    email,
			    created_at
			) VALUES (
			    #{email},
			    #{createdAt}
			)
			""")
	@Options(useGeneratedKeys = true, keyProperty = "userId")
	void insert(User user);

}
