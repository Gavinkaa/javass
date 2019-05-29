#Voici nos différents bonus:
    - Une interface graphique permettant le lancement du jeu
    - Le choix de l'atout ainsi que le fait de chibrer
    - La possibilité de faire des annonces ainsi que les interfaces graphiques correspondantes

##Interface graphique de lancement 
L'interface graphique de lancement permet de choisir si l'on veut lancer une partie localement ou en rejoindre une.
Si l'on choisit de rejoindre une partie, l'interface graphique nous donne l'adresse ip à transmettre à celui qui lancera
la partie sur son ordinateur et il ne reste plus qu'à attendre que l'hôte lance la partie.
Si l'on choisit de créer une partie localement, une nouvelle interface apparaîtra, cette interface permet de choisir quels
types de joueur l'on veut, ainsi que tous leurs attributs respectifs, un seed peut optionnelement être choisi. Un système de validation des arguments 
permet de ne pas devoir relancer le programme si une erreur a été faite lors du choix des attributs.
L'interface permet de revenir en arrière après avoir choisi quel type de partie on voulait lancer et gère la fermeture du socket 
ouvert par exemple dans le cas de remoteMain.

##Le choix de l'atout
L'atout peut-être choisi graphiquement à chaque début de tout de jeu l'on peut déléguer cette tâche à notre coéquipier en chibrant,
les MCTS players choisissent également l'atout à l'aide d'un test de valeur.

##Annonces
A chaque premier pli, chaque joueur à la possibilité de faire une annonce, pour les joueurs graphiques, un bouton permet 
d'afficher l'interface de choix de l'annonce, et pour les MCTS s'en chargent automatiquement. A la fin de chaques premiers plis,
les annonces faites sont affichées, les points bonus obtenu seront affichés en vert sur la gauche du GUI, les points en rouge
représentent la potentielle valeur des annonces non prises en compte. Le score est automatiquement incrémenté de ces annonces.
Les annonces faites par un joueur humain sont detectées et validées, si elles ne sont pas valides, les points attribués seront de 0.

