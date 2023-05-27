#include "AboutWidget.h"
#include "Betacraft.h"

#include <QtWidgets>

extern "C" {
	#include "../core/Network.h"
	#include "../core/FileSystem.h"
	#include "../core/Betacraft.h"
}

QString getCreditsHtml() {
	QString output;
	QTextStream stream(&output);
	stream << "<center>\n";

	stream << "<h3>" << QObject::tr("Developers", "About Credits") << "</h3>\n";
	stream << "<p>Moresteck &lt;<a href='https://github.com/Moresteck'>https://github.com/Moresteck</a>&gt;</p>\n";
	stream << "<p>Kazu &lt;<a href='https://github.com/KazuOfficial'>https://github.com/KazuOfficial</a>&gt;</p>\n";

	stream << "</center>\n";

	return output;
}

QString getLinksHtml() {
	QString output;
	QTextStream stream(&output);

	stream << "<a href=\"https://betacraft.uk/\">Website</a>&nbsp;&nbsp;";
	stream << "<a href=\"https://discord.gg/d4WvXeQ\">Discord</a>&nbsp;&nbsp;";
    stream << "<a href=\"https://github.com/betacraftuk/betacraft-launcher/\">GitHub</a>";

	return output;
}

AboutWidget::AboutWidget(QWidget *parent)
	: QWidget{parent} {
	_layout = new QGridLayout(this);
	_menu = new QTabWidget(this);
	_logo = new QLabel(this);
	_links = new QLabel(this);
	_aboutSection = new QWidget(this);
	_creditsSection = new QWidget(this);
	_licenseSection = new QWidget(this);
	_aboutSectionLayout = new QGridLayout();
	_creditsSectionLayout = new QGridLayout();
	_licenseSectionLayout = new QGridLayout();
	_licenseList = new QTextBrowser(this);
	_creditsList = new QTextBrowser(this);

	QFrame* line = new QFrame();
	line->setFrameShape(QFrame::HLine);
	line->setFrameShadow(QFrame::Sunken);
	line->setLineWidth(1);

	QPixmap pic(":/assets/logo.png");
	_logo->setPixmap(pic);

	_links->setText(getLinksHtml());
	_links->setTextFormat(Qt::RichText);
	_links->setTextInteractionFlags(Qt::TextBrowserInteraction);
	_links->setOpenExternalLinks(true);

	char* os = bc_file_os();
	QString versionString = QString("Version: %1 %2 %3")
		.arg(BETACRAFT_VERSION)
		.arg(QString(os))
		.arg("master");

	free(os);

	_aboutSectionLayout->addWidget(_links, 0, 0, Qt::AlignCenter);
	_aboutSectionLayout->addWidget(new QLabel(versionString), 1, 0, Qt::AlignCenter);
	_aboutSectionLayout->addWidget(new QLabel("© Betacraft 2018-2023"), 2, 0, Qt::AlignCenter);

	_aboutSectionLayout->setAlignment(Qt::AlignTop);
	_aboutSectionLayout->setSpacing(10);
	_aboutSectionLayout->setContentsMargins(10, 10, 10, 10);

	QFile fileCopying(":/COPYING.md");
	if (fileCopying.open(QIODevice::ReadOnly | QIODevice::Text)) {
        _licenseList->setMarkdown(fileCopying.readAll());
	}

	_licenseSectionLayout->setSpacing(0);
	_licenseSectionLayout->setContentsMargins(0, 0, 0, 0);
	_licenseSectionLayout->addWidget(_licenseList, 0, 0);

	_creditsList->setOpenExternalLinks(true);
	_creditsList->setHtml(getCreditsHtml());

	_creditsSectionLayout->setSpacing(0);
	_creditsSectionLayout->setContentsMargins(0, 0, 0, 0);
	_creditsSectionLayout->addWidget(_creditsList, 0, 0);

	_aboutSection->setLayout(_aboutSectionLayout);
	_licenseSection->setLayout(_licenseSectionLayout);
	_creditsSection->setLayout(_creditsSectionLayout);

    _menu->addTab(_aboutSection, bc_translate("about_about_tab"));
    _menu->addTab(_creditsSection, bc_translate("about_credits_tab"));
    _menu->addTab(_licenseSection, bc_translate("about_license_tab"));
	_menu->setStyleSheet("QTabWidget::tab-bar { alignment: center; } QTabWidget:pane { border-top: 1px solid gray; }");

	_layout->setRowMinimumHeight(0, 15);
	_layout->addWidget(_logo, 1, 1, Qt::AlignCenter | Qt::AlignTop);
	_layout->setRowMinimumHeight(2, 15);
	_layout->addWidget(_menu, 3, 1);

	_layout->setSpacing(0);
	_layout->setContentsMargins(5, 5, 5, 5);

	setLayout(_layout);
}
