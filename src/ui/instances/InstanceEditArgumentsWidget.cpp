#include "InstanceEditArgumentsWidget.h"

#include "../Betacraft.h"
#include <QtWidgets>

InstanceEditArgumentsWidget::InstanceEditArgumentsWidget(QWidget* parent)
	: QWidget{ parent }
{
	_layout = new QGridLayout();
	_javaArgumentsGroup = new QGroupBox();
	_programArgumentsGroup = new QGroupBox();
	_javaArgumentsTextEdit = new QTextEdit();
	_programArgumentsTextEdit = new QTextEdit();
	_javaArgumentsLayout = new QGridLayout();
    _programArgumentsLayout = new QGridLayout();
    _separateArgumentsLabel = new QLabel(bc_translate("instance_arguments_advice"), this);

	_javaArgumentsTextEdit->setAcceptRichText(false);
	_programArgumentsTextEdit->setAcceptRichText(false);

	_separateArgumentsLabel->setStyleSheet(".QLabel { font-weight: bold; }");

	_javaArgumentsGroup->setTitle(bc_translate("instance_arguments_jvm_title"));
	_programArgumentsGroup->setTitle(bc_translate("instance_arguments_program_title"));

	_javaArgumentsLayout->addWidget(_javaArgumentsTextEdit);
	_javaArgumentsGroup->setLayout(_javaArgumentsLayout);

	_programArgumentsLayout->addWidget(_programArgumentsTextEdit);
	_programArgumentsGroup->setLayout(_programArgumentsLayout);

	_layout->addWidget(_separateArgumentsLabel, 0, 0, 1, 11);
	_layout->addWidget(_javaArgumentsGroup, 1, 0, 1, 11);
	_layout->addWidget(_programArgumentsGroup, 2, 0, 1, 11);

	_layout->setAlignment(Qt::AlignVCenter);

	_layout->setSpacing(5);
	_layout->setContentsMargins(10, 10, 10, 10);

	setStyleSheet("QLabel { font-size: 14px; }");
	setLayout(_layout);
}

void InstanceEditArgumentsWidget::setInstance(bc_instance instance) {
	_javaArgumentsTextEdit->setText(instance.jvm_args);
	_programArgumentsTextEdit->setText(instance.program_args);
}

bc_instance* InstanceEditArgumentsWidget::getSettings() {
	bc_instance* instance = new bc_instance();

	snprintf(instance->jvm_args, sizeof(instance->jvm_args), "%s", _javaArgumentsTextEdit->toPlainText().toStdString().c_str());
	snprintf(instance->program_args, sizeof(instance->program_args), "%s", _programArgumentsTextEdit->toPlainText().toStdString().c_str());

	return instance;
}
