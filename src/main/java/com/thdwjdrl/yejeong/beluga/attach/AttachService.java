package com.thdwjdrl.yejeong.beluga.attach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.thdwjdrl.yejeong.beluga.common.exception.InvalidRequestException;
import com.thdwjdrl.yejeong.beluga.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachService {

	private static final String EVENT_REPRESENTATIVE_ATTACH_TYPE = "EVENT_REPRESENTATIVE";

	private final AttachMapper attachMapper;
	private final Clock clock;
	private final Path uploadRootPath;

	public AttachService(
			AttachMapper attachMapper,
			Clock clock,
			@Value("${beluga.attach.upload-dir:uploads}") String uploadDir
	) {
		this.attachMapper = attachMapper;
		this.clock = clock;
		this.uploadRootPath = Path.of(uploadDir).toAbsolutePath().normalize();
	}

	@Transactional(readOnly = true)
	public Attach getRequiredAttach(Long attachId) {
		Attach attach = attachMapper.findById(attachId);
		if (attach == null) {
			throw new ResourceNotFoundException("첨부파일을 찾을 수 없습니다.");
		}
		return attach;
	}

	public Attach saveEventRepresentativeImage(MultipartFile file) {
		validateImage(file);

		String originalFileName = extractOriginalFileName(file);
		String storedFileName = buildStoredFileName(originalFileName);
		Path directory = uploadRootPath.resolve("events");
		Path targetPath = directory.resolve(storedFileName);

		try {
			Files.createDirectories(directory);
			file.transferTo(targetPath);
		}
		catch (IOException exception) {
			throw new IllegalStateException("이벤트 이미지를 저장하지 못했습니다.", exception);
		}

		Attach attach = new Attach();
		attach.setAttachType(EVENT_REPRESENTATIVE_ATTACH_TYPE);
		attach.setOriginalFileName(originalFileName);
		attach.setStoredFileName(storedFileName);
		attach.setFilePath(targetPath.toString());
		attach.setContentType(Objects.requireNonNullElse(file.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE));
		attach.setFileSize(file.getSize());
		attach.setCreatedAt(LocalDateTime.now(clock));

		try {
			attachMapper.insert(attach);
			return attach;
		}
		catch (RuntimeException exception) {
			deleteStoredFile(targetPath);
			throw exception;
		}
	}

	public void deleteStoredFile(Attach attach) {
		if (attach == null || attach.getFilePath() == null || attach.getFilePath().isBlank()) {
			return;
		}

		try {
			deleteStoredFile(Path.of(attach.getFilePath()));
		}
		catch (InvalidPathException exception) {
			// Ignore invalid paths during cleanup.
		}
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

	private void validateImage(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new InvalidRequestException("대표 이미지는 필수입니다.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new InvalidRequestException("이미지 파일만 업로드할 수 있습니다.");
		}
	}

	private String extractOriginalFileName(MultipartFile file) {
		String originalFileName = file.getOriginalFilename();
		if (originalFileName == null || originalFileName.isBlank()) {
			throw new InvalidRequestException("파일 이름이 올바르지 않습니다.");
		}

		try {
			return Path.of(originalFileName).getFileName().toString();
		}
		catch (InvalidPathException exception) {
			throw new InvalidRequestException("파일 이름이 올바르지 않습니다.");
		}
	}

	private String buildStoredFileName(String originalFileName) {
		int extensionIndex = originalFileName.lastIndexOf('.');
		String extension = extensionIndex >= 0 ? originalFileName.substring(extensionIndex) : "";
		return UUID.randomUUID() + extension;
	}

	private void deleteStoredFile(Path path) {
		try {
			Files.deleteIfExists(path);
		}
		catch (IOException exception) {
			throw new IllegalStateException("저장된 파일을 정리하지 못했습니다.", exception);
		}
	}

}
