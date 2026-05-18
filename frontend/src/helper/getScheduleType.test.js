import { getScheduleType } from './getScheduleType';
import { FULL, GROUP, TEACHER, DEPARTMENT } from '../constants/scheduleTypes';

describe('getScheduleType function', () => {

    describe('FULL schedule type', () => {
        it('should return FULL when values is empty object', () => {
            // Arrange
            const values = {};
            // Act
            const result = getScheduleType(values);
            // Assert
            expect(result).toEqual(FULL);
        });

        it('should return FULL when group has id: 0 (falsy)', () => {
            // Arrange
            const values = { group: { id: 0 } };
            // Act
            const result = getScheduleType(values);
            // Assert
            expect(result).toEqual(FULL);
        });

        it('should return FULL when group id is null', () => {
            // Arrange
            const values = { group: { id: null } };
            // Act
            const result = getScheduleType(values);
            // Assert
            expect(result).toEqual(FULL);
        });

        it('should return FULL when teacher id is null', () => {
            // Arrange
            const values = { teacher: { id: null } };
            // Act
            const result = getScheduleType(values);
            // Assert
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

        it('should return FULL when nested objects are empty (no id fields)', () => {
            // Arrange
            const values = { group: {}, teacher: {}, department: {} };
            // Act
            const result = getScheduleType(values);
            // Assert
            expect(result).toEqual(FULL);
        });
    });

    describe('GROUP schedule type', () => {
        it('should return GROUP when group has a valid id', () => {
            // Arrange
            const group = { id: 49 };
            // Act
            const result = getScheduleType({ group });
            // Assert
            expect(result).toEqual(GROUP);
        });

        it('should return GROUP when all fields are set but group id is present (priority)', () => {
            // Arrange — group takes priority over teacher and department
            const values = { group: { id: 1 }, teacher: { id: 2 }, department: { id: 3 } };
            // Act
            const result = getScheduleType(values);
            // Assert
            expect(result).toEqual(GROUP);
        });
    });

    describe('TEACHER schedule type', () => {
        it('should return TEACHER when teacher has a valid id', () => {
            // Arrange
            const teacher = { id: 49 };
            // Act
            const result = getScheduleType({ teacher });
            // Assert
            expect(result).toEqual(TEACHER);
        });

        it('should return TEACHER when teacher has id and department has id (teacher has priority)', () => {
            // Arrange
            const values = { teacher: { id: 5 }, department: { id: 10 } };
            // Act
            const result = getScheduleType(values);
            // Assert
            expect(result).toEqual(TEACHER);
        });
    });

    describe('DEPARTMENT schedule type', () => {
        it('should return DEPARTMENT when department has a valid id', () => {
            // Arrange
            const department = { id: 7 };
            // Act
            const result = getScheduleType({ department });
            // Assert
            expect(result).toEqual(DEPARTMENT);
        });

        it('should return DEPARTMENT when only department id is set and others are missing', () => {
            // Arrange
            const values = { group: { id: null }, teacher: {}, department: { id: 99 } };
            // Act
            const result = getScheduleType(values);
            // Assert
            expect(result).toEqual(DEPARTMENT);
        });
    });
});
