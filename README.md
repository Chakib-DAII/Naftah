# Naftah Programming Language : Let's write programs in Arabic, like living in Naftah

#  نفطه: لغة برمجة، لنكتب برامج باللغة العربية وكأننا نعيش في نفطه 

### Motivations

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Official Antlr 4 website](https://antlr.org)
* [Official Antlr 4  documentation](https://github.com/antlr/antlr4/blob/master/doc/getting-started.md)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

### Generate Grammar

To get the lexer and parser generated from the grammar(s), We can simply run:

####  Linux/Mac
```bash
./gradlew generateGrammarSource
```
#### Windows
```bash
gradlew generateGrammarSource
```
Then we can also run:
#### Linux/Mac
```bash
./gradlew idea
```
#### Windows
```bash
gradlew idea
```

### Code Examples

#### Variable declaration
````naftah 
متغير ش تعيين ١
ثابت ت تعيين 2
متغير ع تعيين 15
ثابت ي تعيين ١
````

#### Function call
````naftah 
إطبع("مرحباً أيها العالم!")
````
#### Conditional
````naftah 
متغير أ تعيين ١
متغير ب تعيين 4

إذا أ زائد ب أكبر_من ١٠ إذن { 
إطبع("أ زائد ب أكبر من 10")
} غير_ذلك_إذا أ زائد ب أصغر_من ١٠ إذن { 
إطبع("أ زائد ب أصغر مين 10")
} غير_ذلك { 
إطبع("أ زائد ب يساوي 10")
{
أنهي
````


TODO: enrich and think more about grammars and syntax
add more lexer rules and parser rules 
add Context to have all the programs data, (like a heap and stack simulation)
add support for builtin functions and reuse of java libraries (think about 
how to bind java types and packages to our language and reuse them 
and how to make it open to support any java library in addition to jdk code)
think about how to make this programming language 
 - minimalistic
 - procedural
 - object oriented but simple
 - functional api and lamdas support in the syntax


Ensure that Terminal Supports RTL:
Many modern terminals (e.g., Windows Terminal, iTerm2, GNOME Terminal, and Terminal on macOS) support RTL languages, including Arabic, as long as the system supports the UTF-8 encoding.
Ensure your terminal’s font supports Arabic characters.

for functions from arabic to builtin we can use annotations as marker