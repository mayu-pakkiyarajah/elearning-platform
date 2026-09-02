package com.elearning.certificate.controller;

import com.elearning.certificate.dto.request.GenerateCertificateRequest;
import com.elearning.certificate.dto.response.CertificateResponse;
import com.elearning.certificate.dto.response.CertificateVerificationResponse;
import com.elearning.certificate.entity.Certificate;
import com.elearning.certificate.security.AuthenticatedUser;
import com.elearning.certificate.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
@Tag(name = "Certificates", description = "Generate, list, download, and verify course-completion certificates")
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Generate a certificate for a completed course (idempotent)")
    public ResponseEntity<CertificateResponse> generate(
            @Valid @RequestBody GenerateCertificateRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest httpRequest
    ) {
        String bearerToken = httpRequest.getHeader("Authorization");
        return ResponseEntity.ok(certificateService.generate(request.getCourseId(), currentUser, bearerToken));
    }

    @GetMapping("/mine")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List the current student's certificates")
    public ResponseEntity<List<CertificateResponse>> listMine(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(certificateService.listMine(currentUser));
    }

    @GetMapping("/{id}/download")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Download your own certificate PDF")
    public ResponseEntity<ByteArrayResource> download(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        Certificate certificate = certificateService.getOwnedCertificateOrThrow(id, currentUser);
        return pdfResponse(certificate);
    }

    @GetMapping("/verify/{code}")
    @Operation(summary = "Public: verify a certificate by its code — no login required")
    public ResponseEntity<CertificateVerificationResponse> verify(@PathVariable String code) {
        return ResponseEntity.ok(certificateService.verify(code));
    }

    @GetMapping("/verify/{code}/pdf")
    @Operation(summary = "Public: view/download the PDF for a shared certificate link — no login required")
    public ResponseEntity<ByteArrayResource> downloadShared(@PathVariable String code) {
        Certificate certificate = certificateService.getByVerificationCodeOrThrow(code);
        return pdfResponse(certificate);
    }

    private ResponseEntity<ByteArrayResource> pdfResponse(Certificate certificate) {
        ByteArrayResource resource = new ByteArrayResource(certificate.getPdfData());
        String filename = "certificate-" + certificate.getVerificationCode() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(filename).build().toString())
                .body(resource);
    }
}
