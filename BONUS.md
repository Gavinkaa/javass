#Voici nos différents bonus:
    - Une interface graphique permettant le lancement du jeu
    - Le choix de l'atout ainsi que le fait de chibrer
    - La possibilité de faire des annonces ainsi que les interfaces graphiques correspondantes
    - Enregistrement des paramètres
    - Animations

##Interface graphique de lancement 
L'interface graphique de lancement permet de choisir si l'on veut lancer une partie localement ou en rejoindre une.
Si l'on choisit de rejoindre une partie, l'interface graphique nous donne l'adresse ip à transmettre à celui qui lancera
la partie sur son ordinateur et il ne reste plus qu'à attendre que l'hôte lance la partie.
Si l'on choisit de créer une partie localement, une nouvelle interface apparaîtra, cette interface permet de choisir quels
types de joueur l'on veut, ainsi que tous leurs attributs respectifs, un seed peut optionnelement être choisi. Un système de validation des arguments 
permet de ne pas devoir relancer le programme si une erreur a été faite lors du choix des attributs.
L'interface permet de revenir en arrière après avoir choisi quel type de partie on voulait lancer et gère la fermeture du socket 
ouvert par exemple dans le cas de remoteMain.

####Difficulté
Nous avons eu plusieurs difficultés dans cette étape, la première était de garder le jeu sur la même fenêtre que celle permettant de choisir,
et de ne pas fermer le jeu lorsque les arguments choisis sont faux. Une autre difficulté a été d'implémenter le bouton retour,
en effet, quand on rejoint une partie, le remoteMain se lance et attend une connexion, mais il a fallu pouvoir
fermer cette connexion quand on appuie sur bouton retour.

##Le choix de l'atout
L'atout peut-être choisi graphiquement à chaque début de tout de jeu l'on peut déléguer cette tâche à notre coéquipier en chibrant,
les MCTS players choisissent également l'atout à l'aide d'un test de valeur.

####Difficulté
La principale difficulté pour l'implémentation de l'atout a été de savoir comment transmettre ces nouvelles données, et de pouvoir le
cas échéant chibrer. Il a fallu ajouter une nouvelle JassCommand, une nouvelle blockingQueue et modifier l'interface player, afin de lui ajouter
la méthode de choix d'atout.

##Annonces
A chaque premier pli, chaque joueur à la possibilité de faire une annonce, pour les joueurs graphiques, un bouton permet 
d'afficher l'interface de choix de l'annonce, et pour les MCTS s'en chargent automatiquement, ils font la meilleure annonce possible. A la fin de chaques premiers plis,
les annonces faites sont affichées, les points bonus obtenu seront affichés en vert sur la gauche du GUI, les points en rouge
représentent la potentielle valeur des annonces non prises en compte. Le score est automatiquement incrémenté de ces annonces.
Les annonces faites par un joueur humain sont detectées et validées, si elles ne sont pas valides, les points attribués seront de 0.

####Difficulté
Pour les annonces il a fallu ajouter 2 Jass Command, une pour annoncer les joueurs des annonces des autres et une pour choisir son annonce.
On a dû modifier l'interface de player ainsi que la classe Jass game. On a créé une classe AnnounceValue qui permet d'évaluer les annonces,
une classe announcebean qui permet d'avoir accède en direct aux propriétés. Deux Panes mettent en place graphiquement cet ajout, une pane
permettant de choisir l'annonce et une autre permettant d'afficher les annonces de tout le monde.

##Enregistrement des paramètres
Le programme enregistre les paramètres choisis à chaque lancement de partie, et les pré-remplis à la prochaine exectution du programme.

##Animations
Quelques animations ont étées ajoutées dans l'interface graphique.