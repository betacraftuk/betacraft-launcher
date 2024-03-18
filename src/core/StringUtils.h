#ifndef BC_STRINGUTILS_H
#define BC_STRINGUTILS_H

void repl_str(char *source, char *substring, char *with);
char *repl_str_alloc(char *source, char *substring, char *with, int freeSource);
int count_substring(char *s, char sub);
int str_starts_with(char *string, char *prefix);
int str_ends_with(char *string, char *suffix);

#endif
