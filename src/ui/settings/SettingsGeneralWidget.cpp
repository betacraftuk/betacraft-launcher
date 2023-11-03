#include "SettingsGeneralWidget.h"

#include "../Betacraft.h"
#include <QtWidgets>

extern "C" {
    #include "../../core/Betacraft.h"
}

SettingsGeneralWidget::SettingsGeneralWidget(QWidget* parent)
    : QWidget{ parent } {
    _layout = new QGridLayout(this);
    _languageLayout = new QVBoxLayout();
    _languageGroup = new QGroupBox(this);
    _themeLayout = new QVBoxLayout();
    _themeGroup = new QGroupBox(this);
    _discordLayout = new QVBoxLayout();
    _discordGroup = new QGroupBox("Discord", this);
    _languageCombo = new QComboBox(this);
    _themeCombo = new QComboBox(this);
    _discordCheckbox = new QCheckBox("Discord Rich Presence", this);

    _languageGroup->setTitle(bc_translate("settings_general_language_title"));
    _themeGroup->setTitle(bc_translate("theme"));

    QDir langDir(":/lang/");
    foreach(QString lang, langDir.entryList(QDir::Files)) {
        _languageCombo->addItem(lang);
    }

    QDir themeDir(":/theme/");
    foreach(QString theme, themeDir.entryList(QDir::Files)) {
        _themeCombo->addItem(theme);
    }

    bc_settings* settings = bc_settings_get();
    _languageCombo->setCurrentText(QString(settings->language));
    _themeCombo->setCurrentText(QString(settings->theme));
    _discordCheckbox->setChecked(settings->discord);

    free(settings);

    _languageLayout->addWidget(_languageCombo);
    _languageGroup->setLayout(_languageLayout);

    _themeLayout->addWidget(_themeCombo);
    _themeGroup->setLayout(_themeLayout);

    _discordLayout->addWidget(_discordCheckbox);
    _discordGroup->setLayout(_discordLayout);

    _layout->setSpacing(0);
    _layout->setContentsMargins(5, 5, 5, 0);
    _layout->setAlignment(Qt::AlignTop);

    _layout->addWidget(_languageGroup, 0, 0, 1, 1);
    _layout->addWidget(_themeGroup, 1, 0, 1, 1);
    _layout->addWidget(_discordGroup, 2, 0, 1, 1);

    setLayout(_layout);

    connect(_languageCombo, SIGNAL(currentTextChanged(QString)), this, SLOT(onLanguageChange(QString)));
    connect(_themeCombo, SIGNAL(currentTextChanged(QString)), this, SLOT(onThemeChange(QString)));
    connect(_discordCheckbox, SIGNAL(clicked(bool)), this, SLOT(onDiscordCheckboxClicked(bool)));
}

void SettingsGeneralWidget::onLanguageChange(const QString& lang) {
    bc_settings* settings = bc_settings_get();

    snprintf(settings->language, sizeof(settings->language), "%s", lang.toStdString().c_str());
    bc_settings_update(settings);

    free(settings);

    QApplication::quit();
}

void SettingsGeneralWidget::onThemeChange(const QString &theme) {
    bc_settings* settings = bc_settings_get();

    snprintf(settings->theme, sizeof(settings->theme), "%s", theme.toStdString().c_str());
    bc_settings_update(settings);

    free(settings);

    QApplication::quit();
}

void SettingsGeneralWidget::onDiscordCheckboxClicked(bool checked) {
    bc_settings* settings = bc_settings_get();
    settings->discord = checked;

    bc_settings_update(settings);

    free(settings);

    emit signal_toggleDiscordRPC();
}
