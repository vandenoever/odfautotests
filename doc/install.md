# How to install

## Top-level steps

Required software:

1. Install git
2. Install ant
3. Install Java runtime and compiler

Create the test software locally:

1. Go to the directory where you want to unpack the software
2. At the command-line, type:
        git clone https://github.com/vandenoever/odfautotests
3. This will create a sub-directory called odfautotests

Check that it is running:

1. Change to the odfautotests directory
2. Type at the command-line
        java -jar odftester.jar -t texttests.xml -i input -r output -c config.xml
3. This should have created report.html, which you can check in your browser.


