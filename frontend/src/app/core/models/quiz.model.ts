// ---- Instructor-facing (includes correct answers) ----

export interface Choice {
  id: number;
  text: string;
  position: number;
  correct: boolean;
}

export interface Question {
  id: number;
  text: string;
  position: number;
  points: number;
  choices: Choice[];
}

export interface Quiz {
  id: number;
  courseId: number;
  title: string;
  description?: string;
  passingScorePercent: number;
  questionCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface QuizDetail {
  quiz: Quiz;
  questions: Question[];
}

export interface QuizRequest {
  title: string;
  description?: string;
  passingScorePercent: number;
}

export interface ChoiceRequest {
  text: string;
  position: number;
  correct: boolean;
}

export interface QuestionRequest {
  text: string;
  position: number;
  points: number;
  choices: ChoiceRequest[];
}

// ---- Student-facing (no correct answers) ----

export interface ChoiceTake {
  id: number;
  text: string;
  position: number;
}

export interface QuestionTake {
  id: number;
  text: string;
  position: number;
  points: number;
  choices: ChoiceTake[];
}

export interface QuizTake {
  id: number;
  title: string;
  description?: string;
  passingScorePercent: number;
  questions: QuestionTake[];
}

// ---- Submissions ----

export interface AnswerRequest {
  questionId: number;
  choiceId: number | null;
}

export interface SubmitQuizRequest {
  answers: AnswerRequest[];
}

export interface Submission {
  id: number;
  quizId: number;
  studentId: number;
  attemptNumber: number;
  scorePercent: number;
  passed: boolean;
  submittedAt: string;
}

export interface AnswerResult {
  questionId: number;
  selectedChoiceId: number | null;
  correctChoiceId: number;
  correct: boolean;
}

export interface SubmissionDetail {
  submission: Submission;
  answers: AnswerResult[];
}
