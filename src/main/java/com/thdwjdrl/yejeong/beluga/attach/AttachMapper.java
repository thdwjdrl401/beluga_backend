package com.thdwjdrl.yejeong.beluga.attach;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AttachMapper {

	@Insert("""
			INSERT INTO attach (
			    attach_type,
			    original_file_name,
			    stored_file_name,
			    file_path,
			    content_type,
			    file_size,
			    created_at
			) VALUES (
			    #{attachType},
			    #{originalFileName},
			    #{storedFileName},
			    #{filePath},
			    #{contentType},
			    #{fileSize},
			    #{createdAt}
			)
			""")
	@Options(useGeneratedKeys = true, keyProperty = "attachId")
	void insert(Attach attach);

	@Select("""
			SELECT
			    attach_id,
			    attach_type,
			    original_file_name,
			    stored_file_name,
			    file_path,
			    content_type,
			    file_size,
			    created_at
			FROM attach
			WHERE attach_id = #{attachId}
			""")
	Attach findById(@Param("attachId") Long attachId);

}
