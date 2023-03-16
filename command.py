import os

os.chdir('src/thesisfinal')
os.system('javac -encoding UTF-8 *.java')
os.chdir('../..')
os.system('java -cp src thesisfinal.DhakaSim gauss 1000 1 1 1 1 1')
