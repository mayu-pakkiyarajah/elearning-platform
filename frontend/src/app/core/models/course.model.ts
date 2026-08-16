import { Category } from './category.model';

export type CourseLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type CourseStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type LessonContentType = 'VIDEO' | 'DOCUMENT' | 'TEXT';

export interface Lesson {
  id: number;
  title: string;
  position: number;
  contentType: LessonContentType;
  videoUrl?: string;
  durationSeconds?: number;
  textContent?: string;
  preview: boolean;
}

export interface Section {
  id: number;
  title: string;
  position: number;
  lessons: Lesson[];
}

/** Full detail — course + sections + lessons. From GET /courses/{slug} and /courses/mine. */
export interface Course {
  id: number;
  instructorId: number;
  category?: Category;
  title: string;
  slug: string;
  subtitle?: string;
  description?: string;
  level: CourseLevel;
  language: string;
  price: number;
  thumbnailUrl?: string;
  status: CourseStatus;
  createdAt: string;
  updatedAt: string;
  sections: Section[];
}

/** Lightweight card view for catalog listing. From GET /courses (browse). */
export interface CourseSummary {
  id: number;
  title: string;
  slug: string;
  subtitle?: string;
  category?: Category;
  level: CourseLevel;
  language: string;
  price: number;
  thumbnailUrl?: string;
  status: CourseStatus;
  instructorId: number;
}

export interface CourseRequest {
  title: string;
  subtitle?: string;
  description?: string;
  categoryId?: number | null;
  level: CourseLevel;
  language: string;
  price: number;
  thumbnailUrl?: string;
}

export interface SectionRequest {
  title: string;
  position: number;
}

export interface LessonRequest {
  title: string;
  position: number;
  contentType: LessonContentType;
  videoUrl?: string;
  durationSeconds?: number;
  textContent?: string;
  preview: boolean;
}
