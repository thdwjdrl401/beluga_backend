package com.thdwjdrl.yejeong.beluga.attach;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import com.thdwjdrl.yejeong.beluga.common.exception.ResourceNotFoundException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttachService {

	private final AttachMapper attachMapper;

	public AttachService(AttachMapper attachMapper) {
		this.attachMapper = attachMapper;
	}

	@Transactional(readOnly = true)
	public Attach getRequiredAttach(Long attachId) {
		Attach attach = attachMapper.findById(attachId);
		if (attach == null) {
			throw new ResourceNotFoundException("첨부파일을 찾을 수 없습니다.");
		}
		return attach;
	}

	public Resource loadRequiredResource(Attach attach) {
		try {
			Path path = Path.of(attach.getFilePath());
			if (!Files.exists(path) || !Files.isReadable(path)) {
				throw new ResourceNotFoundException("첨부파일을 찾을 수 없습니다.");
			}
			return new FileSystemResource(path);
		}
		catch (InvalidPathException exception) {
			throw new ResourceNotFoundException("첨부파일을 찾을 수 없습니다.");
		}
	}

	public MediaType resolveMediaType(Attach attach) {
		try {
			return MediaType.parseMediaType(attach.getContentType());
		}
		catch (IllegalArgumentException exception) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	public String resolveDownloadFileName(Attach attach) {
		if (attach.getOriginalFileName() != null && !attach.getOriginalFileName().isBlank()) {
			return attach.getOriginalFileName();
		}
		return attach.getStoredFileName();
	}

}
