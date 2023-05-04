#include "MainWindow.h"

#include <QtWidgets>

extern "C" {
	#include "../core/Network.h"
	#include "../core/Discord.h"
	#include "../core/Betacraft.h"
	#include "../core/JavaInstallations.h"
}

MainWindow::MainWindow(QWidget *parent) :
	QWidget(parent) {
	char tr[256];

	_mainLayout = new QGridLayout(this);
	_menu = new QTabWidget(this);
	_bottomBackground = new QWidget(this);
	_progressBar = new QProgressBar(this);
	_changelog = new QTextEdit(this);
	_gameProgressTimer = new QTimer(this);
	_discordLoopTimer = new QTimer(this);
	_gameProcess = new QProcess(this);
	_messageBox = new QMessageBox(this);
	_logo = new QLabel(_bottomBackground);
	_playButton = new QPushButton(_bottomBackground);
	_instanceLabel = new QLabel(_bottomBackground);
	_accountLabel = new QLabel(_bottomBackground);

	_consoleLog = new ConsoleLogWidget();
    _instanceListWidget = new InstanceListWidget();
    //_serverListWidget = new ServerListWidget();
    _accountsWidget = new AccountListWidget();
    _settingsWidget = new SettingsWidget();
    _aboutWidget = new AboutWidget();

    _changelog->setReadOnly(true);
    _changelog->setStyleSheet("QTextEdit { background-image: url(':/assets/stone.png'); border: 0; color: white; font-size: 15px; padding-left: 10px; }");
    _changelog->viewport()->setCursor(Qt::ArrowCursor);

	_playButton->setFixedWidth(120);
	_playButton->setFixedHeight(50);
	_bottomBackground->setStyleSheet(".QWidget { background-image: url(':/assets/dirt.png'); }");
	_logo->setPixmap(QPixmap(":/assets/logo.png"));
	bc_translate("play_button", tr);
	_playButton->setText(QString(tr));

	_menu->setStyleSheet("QTabWidget::pane { border: 0; }");
	bc_translate("tab_changelog", tr);
	_menu->addTab(_changelog, QString(tr));
	bc_translate("tab_instances", tr);
	_menu->addTab(_instanceListWidget, QString(tr));
	bc_translate("tab_server_list", tr);
    //_menu->addTab(_serverListWidget, QString(tr));
	_menu->addTab(new QWidget(this), QString(tr));
	bc_translate("tab_accounts", tr);
    _menu->addTab(_accountsWidget, QString(tr));
	bc_translate("tab_settings", tr);
	_menu->addTab(_settingsWidget, QString(tr));
	bc_translate("tab_about", tr);
	_menu->addTab(_aboutWidget, QString(tr));
	_menu->addTab(new QWidget(this), "Donate");

	_menu->setTabEnabled(2, 0);

	if (betacraft_online == 0) {
		_menu->setTabEnabled(2, 0);
		_menu->setTabEnabled(3, 0);
	} else {
		char* response = bc_network_get("https://raw.githubusercontent.com/betacraftuk/betacraft-launcher/v2/README.md", NULL);
		_changelog->setMarkdown(QString(response));
		free(response);
	}

	_instanceLabel->setStyleSheet(".QLabel { color: white; padding-bottom: 10px; }");

	_mainLayout->setSpacing(0);
	_mainLayout->setContentsMargins(0, 0, 0, 0);

	_progressBar->setVisible(false);
	_progressBar->setTextVisible(true);
	_progressBar->setAlignment(Qt::AlignCenter);
	_progressBar->setValue(0);
	_progressBar->setRange(0, 100);

	_mainLayout->addWidget(_menu, 0, 0, 1, 11);
	_mainLayout->addWidget(_progressBar, 1, 0, 1, 11);
	_mainLayout->addWidget(_bottomBackground, 2, 0, 1, 11);
	_mainLayout->addWidget(_logo, 2, 1, 1, 1);
	_mainLayout->addWidget(_instanceLabel, 2, 1, 1, 1, Qt::AlignBottom);
	_mainLayout->addWidget(_playButton, 2, 9, 1, 1);

	_mainLayout->setColumnMinimumWidth(10, 30);
	_mainLayout->setColumnMinimumWidth(0, 30);

	_mainLayout->setRowMinimumHeight(0, 100);
	_mainLayout->setRowMinimumHeight(2, 100);

	setLayout(_mainLayout);

	QString winTitle = "Betacraft " + QString("(%1)").arg(BETACRAFT_VERSION);
	setWindowTitle(winTitle);
	resize(850, 480);
	setMinimumSize(850, 480);
	setFocusPolicy(Qt::StrongFocus);

	connect(_gameProgressTimer, SIGNAL(timeout()), this, SLOT(updateGameProgress()));
	connect(_playButton, &QPushButton::released, this, [this]() { launchGame(); });
	connect(_instanceListWidget, SIGNAL(signal_instanceUpdate()), this, SLOT(onInstanceUpdate()));
    connect(_accountsWidget, SIGNAL(signal_accountUpdate()), this, SLOT(onAccountUpdate()));
	connect(_menu, SIGNAL(currentChanged(int)), this, SLOT(onMenuIndexChanged(int)));
	connect(&_watcher, &QFutureWatcher<void>::finished, this, &MainWindow::launchingGameFinished);
	connect(_discordLoopTimer, &QTimer::timeout, this, [this]() { bc_discord_loop(); });
	connect(_settingsWidget, SIGNAL(signal_toggleTabs()), this, SLOT(onToggleTabs()));
	connect(_settingsWidget, SIGNAL(signal_toggleDiscordRPC()), this, SLOT(onToggleDiscordRPC()));

	startDiscordRPC();

	onInstanceUpdate();
	onAccountUpdate();
	updateInstanceLabel();

	if (betacraft_online == 0)
		return;

	char* updateVersion = bc_update_check();

	if (updateVersion != NULL) {
		QString url = "https://github.com/betacraftuk/betacraft-launcher/releases";
		QString message("A new update is available!<br/>Get Betacraft %0 at<br/>");
		message += "<a href='%1'>%1</a>";

		_messageBox->setWindowTitle("Betacraft");
		_messageBox->setText(message.arg(QString(updateVersion)).arg(url));
		_messageBox->setModal(true);
		_messageBox->setTextFormat(Qt::RichText);

		_messageBox->show();
	}

	free(updateVersion);
}

void MainWindow::startDiscordRPC() {
	int discord = bc_discord_init();

	if (discord) {
		_discordLoopTimer->start(2000);
	}
}

void MainWindow::onToggleDiscordRPC() {
	if (_discordLoopTimer->isActive()) {
		_discordLoopTimer->stop();
		bc_discord_stop();
	} else {
		startDiscordRPC();
	}
}

void MainWindow::onMenuIndexChanged(int index) {
	if (index == 6) { // Last tab
		QDesktopServices::openUrl(QUrl("https://www.patreon.com/"));
		_menu->setCurrentIndex(0);
	}
}

void MainWindow::onToggleTabs() {
	_playButton->setDisabled(_playButton->isEnabled());
	_menu->tabBar()->setDisabled(_menu->tabBar()->isEnabled());
}

void MainWindow::launchingGameFinished() {
	_playButton->setDisabled(false);
	_menu->setDisabled(false);

	_progressBar->setVisible(0);
	_progressBar->setValue(0);
	_consoleLog->close();

	onAccountUpdate();
}

void MainWindow::keyPressEvent(QKeyEvent *e) {
	if(e->key() == Qt::Key_Return) {
		launchGame();
	}
}

void MainWindow::updateGameProgress() {
	bc_progress downloadProgress = bc_network_progress;
	_progressBar->setValue(bc_instance_run_progress());

	QString progressString(downloadProgress.filename);
	progressString = progressString.split('/').last();

	if (downloadProgress.nowDownloaded > 0) {
		progressString += " - " + QString::number(downloadProgress.nowDownloadedMb, 'f', 2) + "MB";
	}

	if (downloadProgress.totalToDownload > 0) {
		progressString += " / " + QString::number(downloadProgress.totalToDownloadMb, 'f', 2) + "MB";
	}

	switch (_progressBar->value()) {
		case 25:
			_progressBar->setFormat("Downloading version: " + progressString);
			break;
		case 50:
			_progressBar->setFormat("Downloading libraries: " + progressString);
			break;
		case 75:
			_progressBar->setFormat("Downloading assets: " + progressString);
			break;
		case 100:
			_progressBar->setFormat("Running...");
			_gameProgressTimer->stop();

			if (_instanceSelectedShowLog == 1) {
				_consoleLog->show();
			}
			break;
		default:
			break;
	}

	if (downloadProgress.filename[0] == '\0') {
		_progressBar->setFormat("Running...");
		return;
	}
}

bool MainWindow::recommendedJavaCheck() {
	char* recommendedJavaVersion = bc_jrepo_get_recommended(_instanceSelectedVersion.toStdString().c_str());
	char* selectedJava = bc_jinst_select_get();

    char* parsedRecommended = bc_jrepo_parse_version(recommendedJavaVersion);
    char* parsedSelected = NULL;

	bool startGame = true;

    if (selectedJava != NULL) {
		bc_jinst* jinst = bc_jinst_get(selectedJava);
        parsedSelected = bc_jrepo_parse_version(jinst->version);
		free(jinst);
    } 
	
	if (selectedJava == NULL || strcmp(parsedRecommended, parsedSelected) != 0) {
        bc_jinst_array* jinstList = bc_jinst_get_all();

        char* matchingJavaInstallationPath = NULL;

        for (int i = 0; i < jinstList->len; i++) {
			char* parsedVersion = bc_jrepo_parse_version(jinstList->arr[i].version);

            if (strcmp(parsedRecommended, parsedVersion) == 0) {
                matchingJavaInstallationPath = jinstList->arr[i].path;
				free(parsedVersion);
                break;
            }

			free(parsedVersion);
        }

        QMessageBox messageBox;
        QString message = "Your Java installation is not supported by this version of the game.\n";
        
        message += matchingJavaInstallationPath == NULL
            ? QString("Would you like to install recommended Java %0?").arg(recommendedJavaVersion)
            : QString("Would you like to switch to Java %0?").arg(parsedRecommended);

		messageBox.setWindowTitle("Betacraft");
        messageBox.setText(message);
        messageBox.setStandardButtons(QMessageBox::Yes | QMessageBox::No);

        int ret = messageBox.exec();

        if (ret == QMessageBox::Yes) {
            if (matchingJavaInstallationPath != NULL) {
                bc_jinst_select(matchingJavaInstallationPath);
            } else {
                _menu->setCurrentIndex(4);
                _settingsWidget->downloadRecommendedJava(QString(recommendedJavaVersion));
                startGame = false;
            }
        }

        free(jinstList);
    }

    free(parsedRecommended);
	free(recommendedJavaVersion);

	if (selectedJava != NULL) {
		free(selectedJava);
        free(parsedSelected);
	}

	return startGame;

}

void MainWindow::launchGame() {
	if (!recommendedJavaCheck())
		return;

	_menu->setCurrentIndex(0);
	_playButton->setDisabled(true);
	_menu->setDisabled(true);

	QFuture<void> future = QtConcurrent::run(bc_instance_run, "", "");
	_watcher.setFuture(future);

	_progressBar->setVisible(1);
	_progressBar->setFormat("Reading version json file...");

	_gameProgressTimer->start(1000);

	QString userStatus = "User: Demo";
	if (!_username.isNull()) {
		userStatus = QString("User: %1").arg(_username);
	}

	QString versionStatus = QString("Version: %1").arg(_instanceSelectedVersion);
	bc_discord_activity_update(userStatus.toStdString().c_str(), versionStatus.toStdString().c_str());
}

void MainWindow::onInstanceUpdate() {
	bc_instance* instance = bc_instance_select_get();

	if (instance != NULL) {
		_instanceSelectedVersion = QString(instance->version);
		_instanceSelectedName = QString(instance->name);
		_instanceSelectedShowLog = instance->show_log;

		_playButton->setEnabled(1);
		free(instance);
	} else {
		_instanceSelectedVersion = NULL;
		_instanceSelectedName = NULL;
		_instanceSelectedShowLog = 0;
		_playButton->setEnabled(0);
	}

	updateInstanceLabel();
}

void MainWindow::onAccountUpdate() {
	bc_account* account = bc_account_select_get();

	if (account != NULL) {
		_username = QString(account->username);
		QString activity = QString("User: %1").arg(_username);

		bc_discord_activity_update(activity.toStdString().c_str(), "Testing Betacraft v2");
		free(account);
	} else {
		bc_discord_activity_update("", "Testing Betacraft v2");
	}

	updateInstanceLabel();
}

void MainWindow::updateInstanceLabel() {
	QString label;

	if (!_username.isNull()) {
		label = _username;
	}

	if (!_instanceSelectedVersion.isNull()) {
		if (!_username.isNull()) {
			label += " | ";
		}

		label += QString("%1 (%2)").arg(_instanceSelectedName).arg(_instanceSelectedVersion);
	}

	_instanceLabel->setText(label);
}
