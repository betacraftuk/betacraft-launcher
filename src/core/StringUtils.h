#ifndef BC_STRINGUTILS_H
#define BC_STRINGUTILS_H

char* repl_str(const char* str, const char* from, const char* to);
char** split_str(char* input, char* delimeter);
int count_substring(char* total, char sub);
int str_starts_with(char* string, char* prefix);
int str_ends_with(char* string, char* suffix);

#endif
