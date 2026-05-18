# Coverage Report

## Загальне покриття
- Statements/Instructions: 30.08% (1894/6296)
- Branches: 9.21% (183/1986)
- Functions/Methods: 8.47% (154/1817)
- Lines: 32.03% (1865/5821)

## Аналіз
- Найкраще покриті невеликі pure helper-функції: `src/utils/urlUtils.js`, `src/utils/selectUtils.js`, `src/utils/formUtils.js`, `src/utils/sortStrings.js`. У них майже немає складної логіки, тому тести легко проходять усі гілки й усі варіанти входів.
- Додаткових тестів потребують UI/containers та route guards: `src/router/routes/AccessRoute.js`, `src/components/Auth/Auth.js`, `src/components/TeachersPage/TeachersPage.js`, `src/components/TeacherLessonsPage/LessonsTable/LessonsTable.js`.
- Деякі branches не покриті, бо вони залежать від рідкісних станів: невалідний токен, різні ролі користувача, порожні списки, показ/приховування діалогів, toggle disabled/enabled станів, confirm/delete-потоки, а також альтернативні гілки редіректів. Через це statement coverage ще відносно помірне, але branch coverage дуже низьке.
- Загальна картина така: утиліти й прості модулі тестуються добре, а інтеграційна логіка компонентів і маршрутизації потребує більше сценаріїв з моками та перевірками різних станів.

## Скріншот
<img width="1360" height="624" alt="image" src="https://github.com/user-attachments/assets/df12ffcb-bc84-41e9-922f-e9c95861cabb" />
