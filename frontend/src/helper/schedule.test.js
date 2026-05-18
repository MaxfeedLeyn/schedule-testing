import { divideLessonsByOneHourLesson, getColorByFullness } from './schedule';

// ─── Fixtures ────────────────────────────────────────────────────────────────

const teacherAndSubjectAreTheSame = [
    { subject_for_site: 'Web-дизайн', teacher_for_site: "Куб'як" },
    { subject_for_site: 'Web-дизайн', teacher_for_site: "Куб'як" },
];

const teacherTheSame = [
    { subject_for_site: 'Системи штучного інтелекту', teacher_for_site: 'Мельник' },
    { lesson_type: 'LECTURE', teacher_for_site: 'Мельник' },
];

const teacherIsNotTheSame = [
    { subject_for_site: 'Системи штучного інтелекту', teacher_for_site: 'Мельник' },
    { subject_for_site: 'Інтелектуальні інформаційні системи', teacher_for_site: "Куб'як" },
];

const lessons = [
    { id: 767, hours: 2 },
    { id: 784, hours: 3 },
];

const items = [
    { lesson: { id: 767, hours: 2 } },
    { lesson: { id: 772, hours: 2 } },
];

// ─── divideLessonsByOneHourLesson ─────────────────────────────────────────────

describe('divideLessonsByOneHourLesson', () => {

    describe('Positive scenarios', () => {
        test('should return one-hour lessons whose count equals the sum of hours (no items)', () => {
            // Arrange + Act + Assert
            expect(divideLessonsByOneHourLesson([], lessons).length).toBe(5);
        });

        test('should subtract existing items count from total hours', () => {
            // Arrange + Act + Assert
            expect(divideLessonsByOneHourLesson(items, lessons).length).toBe(4);
        });

        test('should return single entry when lessons contains one element with hours:1', () => {
            // Arrange
            const singleLesson = [{ id: 1, hours: 1 }];
            // Act
            const result = divideLessonsByOneHourLesson([], singleLesson);
            // Assert
            expect(result.length).toBe(1);
        });

        test('should return correct count when all items already match one lesson', () => {
            // Arrange — lesson 767 has hours:2, and 2 items already reference it
            const allItemsForLesson = [
                { lesson: { id: 767, hours: 2 } },
                { lesson: { id: 767, hours: 2 } },
            ];
            const singleLesson = [{ id: 767, hours: 2 }];
            // Act
            const result = divideLessonsByOneHourLesson(allItemsForLesson, singleLesson);
            // Assert — 2 hours - 2 items = 0 slots remaining
            expect(result.length).toBe(0);
        });
    });

    describe('Edge / boundary cases', () => {
        test('should return empty array when lessons is empty', () => {
            // Arrange + Act + Assert
            expect(divideLessonsByOneHourLesson([], [])).toEqual([]);
        });

        test('should return empty array when hours is 0', () => {
            // Arrange
            const zeroHourLesson = [{ id: 1, hours: 0 }];
            // Act
            const result = divideLessonsByOneHourLesson([], zeroHourLesson);
            // Assert
            expect(result.length).toBe(0);
        });

        test('should return empty array when items already fill all available slots', () => {
            // Arrange
            const filledItems = [
                { lesson: { id: 784, hours: 3 } },
                { lesson: { id: 784, hours: 3 } },
                { lesson: { id: 784, hours: 3 } },
            ];
            const filledLesson = [{ id: 784, hours: 3 }];
            // Act
            const result = divideLessonsByOneHourLesson(filledItems, filledLesson);
            // Assert
            expect(result.length).toBe(0);
        });
    });
});

// ─── getColorByFullness ───────────────────────────────────────────────────────

describe('getColorByFullness', () => {

    describe('Positive scenarios', () => {
        test('should return "available" when array is empty', () => {
            // Arrange + Act + Assert
            expect(getColorByFullness([])).toBe('available');
        });

        test('should return "allow" when lesson and teacher are the same', () => {
            // Arrange + Act + Assert
            expect(getColorByFullness(teacherAndSubjectAreTheSame)).toBe('allow');
        });

        test('should return "possible" when teacher is the same but subject differs', () => {
            // Arrange + Act + Assert
            expect(getColorByFullness(teacherTheSame)).toBe('possible');
        });

        test('should return "not-allow" when teacher is not the same', () => {
            // Arrange + Act + Assert
            expect(getColorByFullness(teacherIsNotTheSame)).toBe('not-allow');
        });

        test('should return "allow" for a single-element array', () => {
            // Arrange
            const singleLesson = [{ subject_for_site: 'Math', teacher_for_site: 'Іванов' }];
            // Act + Assert
            expect(getColorByFullness(singleLesson)).toBe('allow');
        });
    });

    describe('Edge / boundary cases', () => {
        test('should return "available" when called with no arguments (default parameter)', () => {
            // Arrange + Act + Assert — relies on default parameter `array = []`
            expect(getColorByFullness()).toBe('available');
        });

        test('should return "not-allow" when teacher changes among three or more entries', () => {
            // Arrange
            const threeEntries = [
                { subject_for_site: 'Math', teacher_for_site: 'Іванов' },
                { subject_for_site: 'Math', teacher_for_site: 'Іванов' },
                { subject_for_site: 'Math', teacher_for_site: 'Петров' }, // teacher changes
            ];
            // Act + Assert
            expect(getColorByFullness(threeEntries)).toBe('not-allow');
        });

        test('should return "possible" when subject changes but teacher stays the same across three entries', () => {
            // Arrange
            const threeEntriesSameTeacher = [
                { subject_for_site: 'Math', teacher_for_site: 'Іванов' },
                { subject_for_site: 'Physics', teacher_for_site: 'Іванов' }, // subject changes
                { subject_for_site: 'Chemistry', teacher_for_site: 'Іванов' }, // subject changes again
            ];
            // Act + Assert
            expect(getColorByFullness(threeEntriesSameTeacher)).toBe('possible');
        });
    });
});
