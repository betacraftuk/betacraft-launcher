#include "MainWindow.h"

#include <QApplication>
#include <QIcon>
#include <QTranslator>

#if defined(__APPLE__)
#include <mach-o/dyld.h>
#include <CoreServices/CoreServices.h>
#elif defined(WIN32) || defined(_WIN32)
#include <direct.h>
#include <process.h>
#endif

#if defined(__linux__) || defined(__APPLE__)
#include <iostream>
#include <unistd.h>
#endif

extern "C" {
	#include "../core/FileSystem.h"
	#include "../core/Betacraft.h"
	#include "../core/Update.h"
	#include "../core/JavaInstallations.h"

#ifdef __APPLE__
    #include "../core/AppleExclusive.h"
#endif
}

void updateCheck() {
	if (QCoreApplication::arguments().contains("-update")) {
		bc_file_directory_copy(".", "../"); // temp -> working directory
		chdir("../");
		execl("Betacraft.exe", "Betacraft.exe", "-updatefinish", NULL);
	}
	else if (QCoreApplication::arguments().contains("-updatefinish")) {
		bc_file_directory_remove("temp");
	}

	//if (betacraft_online == 1) bc_update_perform();
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

#ifdef __APPLE__
void setAppleWorkDir() {
	char* path = bc_file_get_application_support();

	std::string apploc(path);
	apploc.append("/betacraft/");

	make_path(apploc.c_str(), 0);
	chdir(apploc.c_str());

	free(path);
}
#endif

int main(int argc, char *argv[]) {
#ifdef __APPLE__
	setAppleWorkDir();
#else
	make_path("betacraft/", 0);
	chdir("betacraft/");
#endif
	QApplication app(argc, argv);
	app.setWindowIcon(QIcon(":/assets/favicon.ico"));

	copyLanguageFiles();
    copyJavaRepo();
	bc_file_init();
	betacraft_online = bc_network_status();
	updateCheck();
	bc_jinst_system_check();

	MainWindow win;
	win.show();

	return app.exec();
}
