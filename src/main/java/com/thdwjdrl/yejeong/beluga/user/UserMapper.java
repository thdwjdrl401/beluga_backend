package com.thdwjdrl.yejeong.beluga.user;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

	@Select("""
			SELECT
			    user_id,
			    email,
			    password_hash,
			    nickname,
			    role,
			    created_at,
			    updated_at
			FROM users
			WHERE user_id = #{userId}
			""")
	User findById(@Param("userId") Long userId);

	@Select("""
			SELECT
			    user_id,
			    email,
			    password_hash,
			    nickname,
			    role,
			    created_at,
			    updated_at
			FROM users
			WHERE email = #{email}
			""")
	User findByEmail(@Param("email") String email);

	@Insert("""
			INSERT INTO users (
			    email,
			    password_hash,
			    nickname,
			    role,
			    created_at,
			    updated_at
			) VALUES (
			    #{email},
			    #{passwordHash},
			    #{nickname},
			    #{role},
			    #{createdAt},
			    #{updatedAt}
			)
			""")
	@Options(useGeneratedKeys = true, keyProperty = "userId")
	void insert(User user);

	@Update("""
			UPDATE users
			SET nickname = #{nickname},
			    updated_at = #{updatedAt}
			WHERE user_id = #{userId}
			""")
	int updateNickname(
			@Param("userId") Long userId,
			@Param("nickname") String nickname,
			@Param("updatedAt") java.time.LocalDateTime updatedAt
	);

	@Update("""
			UPDATE users
			SET password_hash = #{passwordHash},
			    updated_at = #{updatedAt}
			WHERE user_id = #{userId}
			""")
	int updatePassword(
			@Param("userId") Long userId,
			@Param("passwordHash") String passwordHash,
			@Param("updatedAt") java.time.LocalDateTime updatedAt
	);

}
