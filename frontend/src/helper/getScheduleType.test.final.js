import {getScheduleType} from './getScheduleType';
import {FULL, GROUP, TEACHER, DEPARTMENT} from '../constants/scheduleTypes';

describe('getScheduleType function', () => {

    // ── Original tests (unchanged) ──────────────────────────────────────────
    it('should return type full if values is empty', () => {
        const values = {};
        expect(getScheduleType(values)).toEqual(FULL);
    });
    it('should return type group if group had id', () => {
        const group = { id: 49 };
        expect(getScheduleType({ group })).toEqual(GROUP);
    });
    it('should return type teacher if teacher had id', () => {
        const teacher = { id: 49 };
        expect(getScheduleType({ teacher })).toEqual(TEACHER);
    });

    // ── New tests targeting survived mutants ────────────────────────────────

    describe('DEPARTMENT type — kills ConditionalExpression and StringLiteral mutants on line 13', () => {
        it('should return DEPARTMENT when department has a valid id', () => {
            // Arrange
            const department = { id: 7 };
            // Act
            const result = getScheduleType({ department });
            // Assert — kills mutant: if (false) {} which would return FULL instead
            expect(result).toEqual(DEPARTMENT);
        });

        it('should NOT return DEPARTMENT when department id key is missing', () => {
            // Arrange — department has no "id" key; kills StringLiteral mutant: get(department, "")
            const department = { name: 'CS' };
            // Act
            const result = getScheduleType({ department });
            // Assert
            expect(result).toEqual(FULL);
        });

        it('should return DEPARTMENT with id: 1 (smallest truthy id)', () => {
            // Arrange — further pins that the condition uses get(department, 'id')
            const department = { id: 1 };
            // Act
            const result = getScheduleType({ department });
            // Assert
            expect(result).toEqual(DEPARTMENT);
        });

        it('should return FULL when department object exists but id is 0 (falsy)', () => {
            // Arrange
            const values = { department: { id: 0 } };
            // Act
            const result = getScheduleType(values);
            // Assert — id:0 is falsy for lodash get path check
            expect(result).toEqual(FULL);
        });

        it('should return FULL when department id is null', () => {
            // Arrange
            const values = { department: { id: null } };
            // Act
            const result = getScheduleType(values);
            // Assert
            expect(result).toEqual(FULL);
        });
    });
});
