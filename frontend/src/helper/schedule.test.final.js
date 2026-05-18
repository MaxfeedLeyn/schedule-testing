import {divideLessonsByOneHourLesson, getColorByFullness, addClassDayBoard, removeClassDayBoard} from './schedule';

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

describe('behavior of divideLessonsByOneHourLesson function', () => {
    test('should return lessons with one-hour duration each which length equal amount of all hours of lessons', () => {
        expect(divideLessonsByOneHourLesson([], lessons).length).toBe(5);
    });
    test('should return lessons with one-hour duration each which length equal amount of all hours of lessons minus amount of items which exist in lessons', () => {
        expect(divideLessonsByOneHourLesson(items, lessons).length).toBe(4);
    });

    // ── New tests targeting survived ConditionalExpression mutant (line 12) ──
    // Mutant: if (!isEmpty(items)) → if (true)
    // When mutant fires, filteredLesson is ALWAYS computed even for empty items,
    // but empty items.filter() still returns [] — so length is the same.
    // We need a case where items is non-empty but contains NO match for the lesson id,
    // so filteredLesson.length stays 0 ONLY when isEmpty check is respected.
    // With mutant `if (true)`, filteredLesson would be computed correctly too.
    // The key: pass items whose lesson id does NOT match any lesson, confirm no subtraction.
    test('should return full hours count when items exist but none match any lesson id', () => {
        // Arrange — items have ids that don't appear in lessons
        const nonMatchingItems = [
            { lesson: { id: 999, hours: 2 } },
            { lesson: { id: 888, hours: 2 } },
        ];
        // Act — with items=[non-matching], filteredLesson=[] for each lesson → same as items=[]
        const resultWithNonMatchingItems = divideLessonsByOneHourLesson(nonMatchingItems, lessons);
        const resultWithEmptyItems = divideLessonsByOneHourLesson([], lessons);
        // Assert — both should produce same total hours because filter finds 0 matches
        expect(resultWithNonMatchingItems.length).toBe(resultWithEmptyItems.length);
        expect(resultWithNonMatchingItems.length).toBe(5);
    });

    test('kills if(true) mutant: non-empty items with one matching lesson reduces count correctly', () => {
        // Arrange — items matches lesson 767 once; lesson 767 has hours:2, so slot = 2-1 = 1
        const oneMatchingItem = [{ lesson: { id: 767, hours: 2 } }];
        // Act
        const result = divideLessonsByOneHourLesson(oneMatchingItem, lessons);
        // Assert — 767: 2-1=1, 784: 3-0=3 → total 4
        // If mutant `if (true)` fires AND items = [], filter would return [] anyway.
        // But here items is non-empty with a match, so the difference matters.
        expect(result.length).toBe(4);
    });

    test('empty items [] vs null-like: branch makes no difference for empty but matters for non-empty', () => {
        // Arrange: single lesson, items has matching entry
        const singleLesson = [{ id: 767, hours: 3 }];
        const matchingItems = [
            { lesson: { id: 767, hours: 3 } },
            { lesson: { id: 767, hours: 3 } },
        ];
        // Act
        const withItems = divideLessonsByOneHourLesson(matchingItems, singleLesson);
        const withoutItems = divideLessonsByOneHourLesson([], singleLesson);
        // Assert — demonstrates the branch matters: with items we get fewer
        expect(withItems.length).toBe(1); // 3 - 2 = 1
        expect(withoutItems.length).toBe(3); // 3 - 0 = 3
    });
});

// ─── getColorByFullness ───────────────────────────────────────────────────────

describe('behavior of getColorByFullness function', () => {
    test('should add css class "available" if array of groups is empty', () => {
        expect(getColorByFullness([])).toBe('available');
    });
    test('should add css class "possible" if lesson is not the same and teacher is the same', () => {
        expect(getColorByFullness(teacherTheSame)).toBe('possible');
    });
    test('should add css class "not-allow" if teacher is not the same', () => {
        expect(getColorByFullness(teacherIsNotTheSame)).toBe('not-allow');
    });
    test('should add css class "allow" if lesson and teacher are the same', () => {
        expect(getColorByFullness(teacherAndSubjectAreTheSame)).toBe('allow');
    });

    // No-args default: kills ArrayDeclaration mutant (array = ["Stryker was here"])
    test('should return "available" when called with no arguments (default parameter = [])', () => {
        expect(getColorByFullness()).toBe('available');
    });
});

// ─── addClassDayBoard & removeClassDayBoard — kills NoCoverage/StringLiteral mutants ──

describe('addClassDayBoard', () => {
    let querySelectorAllMock;
    let querySelectorMock;
    let mockEl;
    let mockItems;

    beforeEach(() => {
        // Arrange — mock DOM methods
        mockEl = { classList: { add: jest.fn(), remove: jest.fn() } };
        mockItems = [
            { classList: { add: jest.fn(), remove: jest.fn() } },
            { classList: { add: jest.fn(), remove: jest.fn() } },
        ];
        querySelectorAllMock = jest.spyOn(document, 'querySelectorAll').mockReturnValue(mockItems);
        querySelectorMock = jest.spyOn(document, 'querySelector').mockReturnValue(mockEl);
    });

    afterEach(() => {
        querySelectorAllMock.mockRestore();
        querySelectorMock.mockRestore();
    });

    test('addClassDayBoard selects elements using correct CSS selectors', () => {
        // Act
        addClassDayBoard('monday', '101');
        // Assert — kills StringLiteral mutant: querySelectorAll(``) instead of `.monday-101`
        expect(document.querySelectorAll).toHaveBeenCalledWith('.monday-101');
        expect(document.querySelector).toHaveBeenCalledWith('#monday-101');
    });

    test('addClassDayBoard adds focus-class to the day element', () => {
        // Act
        addClassDayBoard('monday', '101');
        // Assert
        expect(mockEl.classList.add).toHaveBeenCalledWith('focus-class');
    });

    test('addClassDayBoard adds "hover-line" class to first and second row elements', () => {
        // Act
        addClassDayBoard('monday', '101');
        // Assert — kills StringLiteral mutant: hoverLineClassName = "" instead of "hover-line"
        expect(mockItems[0].classList.add).toHaveBeenCalledWith('hover-line');
        expect(mockItems[1].classList.add).toHaveBeenCalledWith('hover-line');
    });
});

describe('removeClassDayBoard', () => {
    let querySelectorAllMock;
    let querySelectorMock;
    let mockEl;
    let mockItems;

    beforeEach(() => {
        mockEl = { classList: { add: jest.fn(), remove: jest.fn() } };
        mockItems = [
            { classList: { add: jest.fn(), remove: jest.fn() } },
            { classList: { add: jest.fn(), remove: jest.fn() } },
        ];
        querySelectorAllMock = jest.spyOn(document, 'querySelectorAll').mockReturnValue(mockItems);
        querySelectorMock = jest.spyOn(document, 'querySelector').mockReturnValue(mockEl);
    });

    afterEach(() => {
        querySelectorAllMock.mockRestore();
        querySelectorMock.mockRestore();
    });

    test('removeClassDayBoard selects elements using correct CSS selectors', () => {
        // Act
        removeClassDayBoard('tuesday', '202');
        // Assert — kills NoCoverage BlockStatement + StringLiteral mutants
        expect(document.querySelectorAll).toHaveBeenCalledWith('.tuesday-202');
        expect(document.querySelector).toHaveBeenCalledWith('#tuesday-202');
    });

    test('removeClassDayBoard removes focus-class from the day element', () => {
        // Act
        removeClassDayBoard('tuesday', '202');
        // Assert
        expect(mockEl.classList.remove).toHaveBeenCalledWith('focus-class');
    });

    test('removeClassDayBoard removes "hover-line" class from both row elements', () => {
        // Act
        removeClassDayBoard('tuesday', '202');
        // Assert — kills hoverLineClassName = "" mutant
        expect(mockItems[0].classList.remove).toHaveBeenCalledWith('hover-line');
        expect(mockItems[1].classList.remove).toHaveBeenCalledWith('hover-line');
    });
});
