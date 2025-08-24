# jlox

This is my implementation of the Lox interpreter from **Robert Nystrom’s Crafting Interpreters**.

My goal wasn’t to build the most performant interpreter, but rather a clean, readable, and modern one. Since an interpreter is mostly about working with structured data and managing the many relationships between them, it felt like a great use case for Java’s new data oriented features, especially pattern matching, which wasn’t available when the book was originally written. I also see this as an ongoing project, whenever Java introduces new features that fit naturally here, I plan to update the interpreter.

I found this project really fun, and it has been a great way to learn the fundamentals of how interpreted, dynamically typed programming languages work, while also teaching me a lot of useful programming concepts along the way.

---

## Current state

The lexer, parser, and interpreter backbone are fully implemented, providing a solid foundation for introducing new language features as I continue reading this book.

### Currently supported

- Numerical operations with full operator precedence and associativity
- Variable assignment
- Print statement
