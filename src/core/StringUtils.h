#ifndef BC_STRINGUTILS_H
#define BC_STRINGUTILS_H

void repl_str(char* target, const char* str, const char* from, const char* to);
int count_substring(char* s, char sub);
int str_starts_with(char* string, char* prefix);
int str_ends_with(char* string, char* suffix);

#endif
