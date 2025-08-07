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
### 🤝 Contributing

We welcome contributions of all kinds!
Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

By participating, you agree to follow our [Code of Conduct](CODE_OF_CONDUCT.md).

### Code Examples

#### Variable declaration and assignment
````naftah
ثابت ت تعيين 2
متغير ش
ش تعيين ١
متغير ع تعيين 15
ثابت ي
تعيين ١
إطبع("ت = ${ت}، ش = ${ش}، ع = ${ع}، ي = ${ي}")
````

#### Function declaration
````naftah
دالة دالة_وهمية(ثابت معلمة_وهمية_1 :أي_نوع تعيين 1، ثابت معلمة_وهمية_2 : تسلسل_أحرف = «أي نص وهمي مع ش = ${ش}»، معلمة_وهمية_3 ، معلمة_وهمية_4 : عدد_عائم_طويل ، معلمة_وهمية_5 : عدد_عائم_طويل =١٣٤٤٥٦،٦٦٦ ) {
ارجع
}

دالة_وهمية()
دالة_وهمية(٧٦٧٤٫٩٥٥)
دالة_وهمية(٧٦٧٤٫٩٥٥ ، ٦٤٤٥٦٬٦٦٦٨)
دالة_وهمية(٧٦٧٤٫٩٥٥ ، «أي نص وهمي»)
دالة_وهمية(٧٦٧٤٫٩٥٥ ، ٦٤٤٥٦٬٦٦٦٨ ، «أي نص وهمي»)
دالة_وهمية(معلمة_وهمية_1 تعيين 1، معلمة_وهمية_2 = «أي نص وهمي مع ش = ${ش}»، معلمة_وهمية_3 = ٦٤٤٥٦٬٦٦٦٨، معلمة_وهمية_4 = «أي نص وهمي»، معلمة_وهمية_5 =١٣٤٤٥٦،٦٦٦)
دالة_وهمية(معلمة_وهمية_1 تعيين 1، معلمة_وهمية_2 = «أي نص وهمي مع ش = ${ش}»، «أي نص وهمي» ، ٧٦٧٤٫٩٥٥ ، معلمة_وهمية_5 =١٣٤٤٥٦،٦٦٦)
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

#### String interpolation
````naftah
ثابت ت تعيين 2
متغير ش
ش تعيين ١
إطبع("ت = ${}،ش = ${}")
````
