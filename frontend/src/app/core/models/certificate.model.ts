export interface Certificate {
  id: number;
  courseId: number;
  courseTitle: string;
  studentName: string;
  verificationCode: string;
  issuedAt: string;
  /** Public link anyone can open to verify + view this certificate, no login required. */
  verificationUrl: string;
}

/** Returned from the public verify endpoint — deliberately minimal, no internal ids. */
export interface CertificateVerification {
  valid: boolean;
  studentName: string;
  courseTitle: string;
  issuedAt: string;
  verificationCode: string;
}
