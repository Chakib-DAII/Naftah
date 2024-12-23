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
متغير ت تعيين 2
متغير ع تعيين 15
متغير ي تعيين ١
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