export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
  instructorApproved: boolean;
}

export type AppRole = 'ROLE_STUDENT' | 'ROLE_INSTRUCTOR' | 'ROLE_ADMIN';
