This is a draft of the documentation.

To install sphinx:
sudo apt-get install python-pip
sudo pip install Sphinx

1. To generate html docs for developers
make SPHINXOPTS="-t developers" html

2. To generate html docs for clients
make SPHINXOPTS="-t clients" html

Open in a browser file: _build/html/index.html
