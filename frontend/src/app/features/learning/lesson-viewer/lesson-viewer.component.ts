import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Course, Lesson } from '../../../core/models/course.model';
import { EnrollmentDetail } from '../../../core/models/enrollment.model';
import { Quiz } from '../../../core/models/quiz.model';
import { CourseService } from '../../../core/services/course.service';
import { EnrollmentService } from '../../../core/services/enrollment.service';
import { QuizService } from '../../../core/services/quiz.service';

interface FlatLesson {
  lesson: Lesson;
  sectionTitle: string;
}

@Component({
  selector: 'app-lesson-viewer',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './lesson-viewer.component.html',
  styleUrl: './lesson-viewer.component.scss',
})
export class LessonViewerComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private courseService = inject(CourseService);
  private enrollmentService = inject(EnrollmentService);
  private quizService = inject(QuizService);

  readonly quizzes = signal<Quiz[]>([]);

  readonly course = signal<Course | null>(null);
  readonly detail = signal<EnrollmentDetail | null>(null);
  readonly isLoading = signal(true);
  readonly notEnrolled = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly currentLessonId = signal<number | null>(null);
  readonly isMarking = signal(false);

  readonly flatLessons = computed<FlatLesson[]>(() => {
    const course = this.course();
    if (!course) return [];
    return course.sections.flatMap((section) =>
      section.lessons.map((lesson) => ({ lesson, sectionTitle: section.title })),
    );
  });

  readonly currentLesson = computed<FlatLesson | null>(() => {
    const id = this.currentLessonId();
    return this.flatLessons().find((fl) => fl.lesson.id === id) ?? this.flatLessons()[0] ?? null;
  });

  readonly completedIds = computed<Set<number>>(() => new Set(this.detail()?.completedLessonIds ?? []));

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (!slug) {
      this.errorMessage.set('Course not found.');
      this.isLoading.set(false);
      return;
    }

    this.courseService.getBySlug(slug).subscribe({
      next: (course) => {
        this.course.set(course);
        this.loadEnrollment(course.id);
      },
      error: () => {
        this.errorMessage.set('Course not found.');
        this.isLoading.set(false);
      },
    });
  }

  private loadEnrollment(courseId: number): void {
    this.enrollmentService.getDetail(courseId).subscribe({
      next: (detail) => {
        this.detail.set(detail);
        this.isLoading.set(false);
        this.selectFirstIncompleteLesson();
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 404) {
          this.notEnrolled.set(true);
        } else {
          this.errorMessage.set('Could not load your progress for this course.');
        }
      },
    });

    this.quizService.listQuizzesForCourse(courseId).subscribe({
      next: (quizzes) => this.quizzes.set(quizzes),
      error: () => {}, // non-fatal — the lesson viewer still works without the quiz list
    });
  }

  private selectFirstIncompleteLesson(): void {
    const completed = this.completedIds();
    const firstIncomplete = this.flatLessons().find((fl) => !completed.has(fl.lesson.id));
    this.currentLessonId.set((firstIncomplete ?? this.flatLessons()[0])?.lesson.id ?? null);
  }

  selectLesson(lessonId: number): void {
    this.currentLessonId.set(lessonId);
  }

  isCompleted(lessonId: number): boolean {
    return this.completedIds().has(lessonId);
  }

  toggleComplete(): void {
    const course = this.course();
    const current = this.currentLesson();
    if (!course || !current) return;

    this.isMarking.set(true);
    const action = this.isCompleted(current.lesson.id)
      ? this.enrollmentService.markLessonIncomplete(course.id, current.lesson.id)
      : this.enrollmentService.markLessonComplete(course.id, current.lesson.id);

    action.subscribe({
      next: (detail) => {
        this.detail.set(detail);
        this.isMarking.set(false);
        this.goToNextIfJustCompleted(current.lesson.id, detail);
      },
      error: () => this.isMarking.set(false),
    });
  }

  private goToNextIfJustCompleted(lessonId: number, detail: EnrollmentDetail): void {
    if (!detail.completedLessonIds.includes(lessonId)) return; // was unmarked, don't advance
    const lessons = this.flatLessons();
    const index = lessons.findIndex((fl) => fl.lesson.id === lessonId);
    const next = lessons[index + 1];
    if (next) {
      this.currentLessonId.set(next.lesson.id);
    }
  }

  goToPrevious(): void {
    const lessons = this.flatLessons();
    const index = lessons.findIndex((fl) => fl.lesson.id === this.currentLessonId());
    if (index > 0) {
      this.currentLessonId.set(lessons[index - 1].lesson.id);
    }
  }

  goToNext(): void {
    const lessons = this.flatLessons();
    const index = lessons.findIndex((fl) => fl.lesson.id === this.currentLessonId());
    if (index >= 0 && index < lessons.length - 1) {
      this.currentLessonId.set(lessons[index + 1].lesson.id);
    }
  }
}
