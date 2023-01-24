(function ($) {
	$(function () {
		// Insert the button and the script onto the page
		$('<button id="zammad-feedback-form" style="position: fixed; right: 0; bottom: 0;">Send feedback</button>').appendTo("body");
		$('<script id="zammad_form_script" src="https://eosc-helpdesk.eosc-portal.eu/assets/form/form.js"></script>').appendTo("body");

		// Set up the form
		setTimeout(setupForm);
	});
})(jQuery);

/**
 * Setup the EOSC feedback form
 */
function setupForm() {

	if (typeof $('#zammad-feedback-form').ZammadForm !== "function") {
		// If ZammadForm is not a function then wait until the setup script has run
		setTimeout(setupForm);
		return;
	}

	// Configure the form
	$('#zammad-feedback-form').ZammadForm({
		agreementMessage: '  Accept CESSDA <a target="_blank" href="https://www.cessda.eu/Privacy-policy">Data Privacy Policy</a> & <a target="_blank" href="https://www.cessda.eu/Acceptable-Use-Policy">Acceptable Use Policy</a>',
		messageTitle: 'CMV Feedback Form',
		messageSubmit: 'Submit',
		messageThankYou: 'Thank you for your inquiry (#%s)! We\'ll contact you as soon as possible.',
		modal: true,
		targetGroupID: 54,
		attributes: [
			{
				display: 'Name',
				name: 'name',
				tag: 'input',
				type: 'text',
				id: 'zammad-form-name',
				required: true,
				placeholder: 'Your Name',
				defaultValue: '',
			},
			{
				display: 'Email',
				name: 'email',
				tag: 'input',
				type: 'email',
				id: 'zammad-form-email',
				required: true,
				placeholder: 'Your Email',
				defaultValue: '',
			},
			{
				display: 'Subject',
				name: 'title',
				tag: 'input',
				id: 'zammad-form-subject',
				required: false,
				placeholder: 'My subject',
				defaultValue: '',
			},
			{
				display: 'Message',
				name: 'body',
				tag: 'textarea',
				id: 'zammad-form-body',
				required: true,
				placeholder: 'Your Message...',
				defaultValue: '',
				rows: 7,
			}
		]
	});
}
