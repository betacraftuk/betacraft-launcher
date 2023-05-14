#include "MainWindow.h"

#include <QApplication>
#include <QIcon>
#include <QTranslator>

#ifdef __APPLE__
#include <mach-o/dyld.h>
#include <CoreServices/CoreServices.h>
#elif _WIN32
#include <direct.h>
#include <process.h>
#endif

#if defined(__linux__) || defined(__APPLE__)
#include <iostream>
#include <unistd.h>
#include <pwd.h>
#endif

extern "C" {
	#include "../core/FileSystem.h"
	#include "../core/Betacraft.h"
	#include "../core/JavaInstallations.h"
#ifdef __APPLE__
    #include "../core/AppleExclusive.h"
#endif
}

void copyLanguageFiles() {
	char* workDir = bc_file_directory_get_working();

	QString langRelative("lang/");

	QDir dir = QDir(QString(workDir));
	dir.mkdir(langRelative);

	QString langPath = dir.absoluteFilePath(langRelative);
	QString langRes(":/lang/");

	QDir langreSourceDir(langRes);

	foreach(QString f, langreSourceDir.entryList(QDir::Files)) {
		QString source = QString(langRes + QDir::separator() + f);
		QString dest = QString(langPath + f + QString(".json"));

		if (QFile::exists(dest)) {
			QFile::remove(dest);
		}

		QFile::copy(source, dest);
	}

	free(workDir);
}

void copyJavaRepo() {
    char* workDir = bc_file_directory_get_working();

    QDir dir = QDir(QString(workDir));
    QString source(":/java_repo.json");
    QString dest = dir.absoluteFilePath(QString("java_repo.json"));

    QFile::copy(source, dest);
	free(workDir);
}

void setWorkDir() {
#ifdef __APPLE__
	char* path = bc_file_get_application_support();

	std::string apploc(path);
	apploc.append("/betacraft/");

	make_path(apploc.c_str(), 0);
	chdir(apploc.c_str());

	free(path);
#elif __linux__
	char workDir[PATH_MAX];
	struct passwd* pw = getpwuid(getuid());
	strcpy(workDir, pw->pw_dir);
	strcat(workDir, "/.local/share/betacraft/");

	make_path(workDir, 0);
	chdir(workDir);
#elif _WIN32
	make_path("betacraft/", 0);
	chdir("betacraft/");
#endif
}

int main(int argc, char *argv[]) {
	setWorkDir();

	QApplication app(argc, argv);
	app.setWindowIcon(QIcon(":/assets/favicon.ico"));

	copyLanguageFiles();
    copyJavaRepo();
	bc_file_init();
	betacraft_online = bc_network_status();
    application_support_path = bc_file_get_application_support();
	bc_jinst_system_check();

	MainWindow win;
	win.show();

	return app.exec();
}
