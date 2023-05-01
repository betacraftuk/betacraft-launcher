#ifdef _WIN32
#include "WindowsProcessHandler.h"

#include "Logger.h"

#include <windows.h>
#include <stdio.h>

#define BUFSIZE 4096

HANDLE g_hChildStd_OUT_Rd = NULL;
HANDLE g_hChildStd_OUT_Wr = NULL;
HANDLE g_hChildStd_ERR_Rd = NULL;
HANDLE g_hChildStd_ERR_Wr = NULL;

PROCESS_INFORMATION CreateChildProcess(bc_process_args* args);
void ReadFromPipe(PROCESS_INFORMATION);

char* bc_winprocess_get_args(bc_process_args* args) {
    int size = args->size + (args->len * 3);
    char* cmd = malloc(args->size + (args->len * 3));
    strcpy(cmd, "\"");
    strcat(cmd, args->arr[0]);
    strcat(cmd, "\"");

    for (int i = 1; i < args->len; i++) {
        strcat(cmd, " \"");
        strcat(cmd, args->arr[i]);
        strcat(cmd, "\"");
    }

    return cmd;
}

void bc_winprocess_create(bc_process_args* args) {
    STARTUPINFO si;
    PROCESS_INFORMATION pi;

    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    ZeroMemory(&pi, sizeof(pi));

    char* cmd = bc_winprocess_get_args(args);

    if (!CreateProcess(NULL,
                       cmd,
                       NULL,
                       NULL,
                       FALSE,
                       0,
                       NULL,
                       NULL,
                       &si,
                       &pi)
        )
    {
        bc_log("CreateProcess failed (%d).\n", GetLastError());
        return;
    }

    WaitForSingleObject(pi.hProcess, INFINITE);

    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);

    free(cmd);
}

void bc_winprocess_create_log(bc_process_args* args) {
    SECURITY_ATTRIBUTES sa;

    sa.nLength = sizeof(SECURITY_ATTRIBUTES);
    sa.bInheritHandle = TRUE;
    sa.lpSecurityDescriptor = NULL;

    if (!CreatePipe(&g_hChildStd_ERR_Rd, &g_hChildStd_ERR_Wr, &sa, 0)) {
        exit(1);
    }

    if (!SetHandleInformation(g_hChildStd_ERR_Rd, HANDLE_FLAG_INHERIT, 0)) {
        exit(1);
    }

    if (!CreatePipe(&g_hChildStd_OUT_Rd, &g_hChildStd_OUT_Wr, &sa, 0)) {
        exit(1);
    }

    if (!SetHandleInformation(g_hChildStd_OUT_Rd, HANDLE_FLAG_INHERIT, 0)) {
        exit(1);
    }

    PROCESS_INFORMATION piProcInfo = CreateChildProcess(args);
    ReadFromPipe(piProcInfo);
}

PROCESS_INFORMATION CreateChildProcess(bc_process_args* args) {
    PROCESS_INFORMATION piProcInfo;
    STARTUPINFO siStartInfo;
    int bSuccess = FALSE;

    ZeroMemory(&piProcInfo, sizeof(PROCESS_INFORMATION));

    ZeroMemory(&siStartInfo, sizeof(STARTUPINFO));
    siStartInfo.cb = sizeof(STARTUPINFO);
    siStartInfo.hStdError = g_hChildStd_ERR_Wr;
    siStartInfo.hStdOutput = g_hChildStd_OUT_Wr;
    siStartInfo.dwFlags |= STARTF_USESTDHANDLES;

    char* cmd = bc_winprocess_get_args(args);

    bSuccess = CreateProcess(NULL,
                             cmd,
                             NULL,
                             NULL,
                             TRUE,
                             0,
                             NULL,
                             NULL,
                             &siStartInfo,
                             &piProcInfo);
    CloseHandle(g_hChildStd_ERR_Wr);
    CloseHandle(g_hChildStd_OUT_Wr);

    free(cmd);

    if (!bSuccess) {
        exit(1);
    }

    return piProcInfo;
}

void ReadFromPipe(PROCESS_INFORMATION piProcInfo) {
    DWORD dwRead;
    CHAR chBuf[BUFSIZE];
    int bSuccess = FALSE;

    for (;;) {
        bSuccess = ReadFile(g_hChildStd_OUT_Rd, chBuf, BUFSIZE, &dwRead, NULL);
        if (!bSuccess || dwRead == 0) break;
        chBuf[dwRead] = '\0';

        if (bc_process_log != NULL) {
            char* newLog = malloc(strlen(bc_process_log) + dwRead + 1);
            strcpy(newLog, bc_process_log);
            strcat(newLog, chBuf);

            free(bc_process_log);
            bc_process_log = newLog;
        } else {
            bc_process_log = malloc(dwRead + 1);
            strcpy(bc_process_log, chBuf);
        }
    }
}
#endif
