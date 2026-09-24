package com.example.data

data class BookPage(
    val pageNumber: Int,
    val bookPageLabel: String,
    val chapterTitle: String = "",
    val sectionTitle: String = "",
    val paragraphs: List<String>,
    val isIllustration: Boolean = false,
    val illustrationType: String = "" // "FORGE" for page 32, "SPIRAL" for page 54
)

object BookRepository {
    val pages: List<BookPage> = listOf(
        BookPage(
            pageNumber = 1,
            bookPageLabel = "",
            chapterTitle = "Les Luminautes",
            sectionTitle = "La Voie de Lumière",
            paragraphs = listOf(
                "Les Luminautes",
                "La Voie de Lumière"
            )
        ),
        BookPage(
            pageNumber = 2,
            bookPageLabel = "",
            paragraphs = emptyList()
        ),
        BookPage(
            pageNumber = 3,
            bookPageLabel = "2",
            chapterTitle = "Prologue",
            paragraphs = listOf(
                "Ressentez-vous parfois ce léger décalage ? L'intuition que derrière le bruit du monde, une autre musique se joue, plus subtile, plus essentielle ? La sensation que les réponses que l'on nous propose ne correspondent plus vraiment aux questions que notre âme se pose ?",
                "Les pages qui suivent ne sont pas un livre de réponses toutes faites. Elles sont le carnet de bord d'une quête. Une traversée de plus d'une décennie, des premières intuitions philosophiques d'un jeune homme jusqu'à l'épreuve du feu d'un adulte, où les idées ont dû être forgées pour survivre.",
                "Ce n'est pas un traité, mais un témoignage. Le récit d'une pensée qui a exploré la physique pour y trouver de la poésie, qui a affronté ses propres ténèbres pour y trouver une lumière, et qui a dialogué avec les sagesses du monde pour trouver sa propre voix.",
                "Cette quête a mené à une vision, celle d'une voie possible, portée par un nom : les Luminautes. Ce manifeste n'est pas un dogme à croire. C'est une porte que nous laissons entre-ouverte, d'où parviennent déjà quelques rayons d'une lumière chaleureuse.",
                "La clé est en vous. La porte est ce livre. Nous vous invitons à faire le premier pas."
            )
        ),
        BookPage(
            pageNumber = 4,
            bookPageLabel = "",
            paragraphs = emptyList()
        ),
        BookPage(
            pageNumber = 5,
            bookPageLabel = "",
            chapterTitle = "Table des matières",
            paragraphs = listOf(
                "Table des matières",
                "1. Les Fondations\n  1.1. Les Illusions de l'Appartenance et du Conflit\n  1.2. Le Trouble des Repères\n  1.3. La Fièvre du Monde : Une leçon de Thermodynamique\n  1.4. Le Rêve comme Boussole, non comme Refuge",
                "2. L'Art de la Juste Maîtrise\n  2.1. Le Baromètre de la Conscience\n  2.2. Les Chemins de la Pensée : Le Cercle et le Tourbillon\n  2.3. Le Chemin de la Sagesse",
                "3. La Clarté et l'Épreuve\n  3.1. L'Éveil\n  3.2. L'espoir d'un fou\n  3.3. Forge Tous Risques !",
                "4. La Cristallisation d'une Vision\n  4.1. En ces temps...\n  4.2. La Naissance d'une Idée\n  4.3. Les Trois Piliers de la Voie\n  4.4. Le Dialogue des Sagesses",
                "5. L'Épreuve du Feu\n  5.1. Le Combat : Le Laboratoire de l'Âme\n  5.2. La Clé : La Spirale de l'Épreuve",
                "6. Derrière la Porte : La Vision Clarifiée\n  6.1. La Mission et la Cosmologie\n  6.2. Le Mécanisme des Luminautes\n  6.3. Le Manifeste du Passeur de Lumière"
            )
        ),
        BookPage(
            pageNumber = 6,
            bookPageLabel = "",
            paragraphs = emptyList()
        ),
        BookPage(
            pageNumber = 7,
            bookPageLabel = "3",
            chapterTitle = "1. Les Fondations",
            sectionTitle = "1.1 Les Illusions de l'Appartenance et du Conflit",
            paragraphs = listOf(
                "Le besoin humain d'appartenir à un groupe est une force puissante. Pourtant, lorsque cette quête se fait sans réflexion personnelle, elle peut mener à une identité d'emprunt. Que ce soit en adoptant une posture de \"lascar\" ou de \"hippie\", beaucoup finissent par épouser des concepts sans les sonder, se basant sur des \"on dit que...\" ou l'idée qu'il \"fait bien\" d'être d'une certaine manière. Cette appartenance de façade, qui se prétend communautaire, révèle souvent une nature plus utopiste qu'authentique.",
                "Cette superficialité est souvent nourrie par une mécanique de pensée plus profonde : la logique du conflit. Une tendance s'observe à vouloir opposer systématiquement les concepts : \"Énergie renouvelable VS Nucléaire\" ou, plus fondamentalement, \"Science VS Spiritualité\". Ces réflexions, présentées comme des combats \"Ceci VS Cela\", cessent d'être une recherche de"
            )
        ),
        BookPage(
            pageNumber = 8,
            bookPageLabel = "4",
            paragraphs = listOf(
                "vérité pour devenir une lutte pour la suprématie d'un camp. L'objectif n'est plus la compréhension d'une idéologie, mais simplement de \"gagner ce combat\", au détriment d'une juste appréciation de la complexité des choses.",
                "Ce mode de pensée, basé sur le conflit et un manque de connaissances, est une impasse. Il enferme l'esprit dans ce que nous avons appelé un \"cercle\" : il tourne en rond, ressassant les mêmes oppositions sans jamais progresser. C'est une pensée stressée, dont le lancer est trop rapide et qui manque cruellement de Sagesse.",
                "La voie Luminaute propose une alternative. Elle invite à remplacer la soif de \"combats, conflits et victoire\" par une insatiable soif de connaissance, de tout type. C'est cette quête de Lumière qui permet d'avoir le \"déclic\", de couper le cercle stérile pour le déployer en un \"tourbillon\" fertile. C'est un appel à cesser de se poser trop peu de questions ou de se contenter de réponses rapides, pour au contraire pousser la réflexion toujours plus loin.",
                "Certes, un monde où chacun abandonnerait le conflit pour la quête de la Sagesse peut sembler utopique. Mais l'utopisme, qui prône l'élévation de l'esprit par la connaissance, n'est pas qu'un rêve inaccessible. Pour le Luminaute, il n'est pas une destination, mais une direction."
            )
        ),
        BookPage(
            pageNumber = 9,
            bookPageLabel = "5",
            paragraphs = listOf(
                "C'est la pratique consciente et quotidienne de transformer ses propres cercles de pensée en spirales de Sagesse."
            )
        ),
        BookPage(
            pageNumber = 10,
            bookPageLabel = "",
            paragraphs = emptyList()
        ),
        BookPage(
            pageNumber = 11,
            bookPageLabel = "6",
            sectionTitle = "1.2 Le Trouble des Repères",
            paragraphs = listOf(
                "Une confusion subtile semble s'être immiscée dans notre époque, particulièrement au sein de la jeunesse. Elle prend la forme d'une inversion des valeurs, où ce qui est \"mauvais\" ou destructeur peut être perçu comme désirable ou \"bien\". Ce trouble des repères fondamentaux engendre un profond désarroi, une perte de direction qui laisse les esprits sans boussole.",
                "La source de ce désarroi se nourrit souvent d'une pression sociale qui dévalorise la connaissance. Une peur s'installe, celle de paraître \"intello\", et pousse à rejeter ce qui sort de l'ordinaire ou ce qui est jugé trop \"intelligent\". Il est pourtant vital de faire la distinction : l'intelligence véritable est une flamme, une curiosité vivante qui cherche à comprendre le monde et soi-même ; sa caricature, l'intellectualisme stérile, n'est qu'une parure froide. Craindre l'intelligence, c'est craindre la Lumière elle-même. C'est refuser de dire et d'entendre des choses qui élèvent.",
                "Cette frustration et ce manque de repères sont amplifiés par un besoin naturel et puissant : celui d'appartenir à un groupe. Pour être accepté, beaucoup adhèrent à ces concepts inversés, se laissant influencer par un noyau d'individus qui dictent les codes. Cette"
            )
        ),
        BookPage(
            pageNumber = 12,
            bookPageLabel = "7",
            paragraphs = listOf(
                "dynamique, qui peut parfois rappeler celle de l'extrémisme, détourne la quête légitime d'amour et de connexion vers une validation de groupe qui empêche l'épanouissement personnel.",
                "Reconnaître ce trouble n'est pas un jugement, mais un premier pas vers la clarté. C'est l'invitation à retrouver de véritables repères, non pas dans le regard des autres, mais au sein de son propre for intérieur, là où la Lumière attend patiemment d'être redécouverte."
            )
        ),
        BookPage(
            pageNumber = 13,
            bookPageLabel = "8",
            sectionTitle = "1.3 La Fièvre du Monde : Une Leçon de Thermodynamique",
            paragraphs = listOf(
                "Pour comprendre les déséquilibres profonds de certains de nos systèmes sociaux, il est parfois utile de les observer à travers le prisme des lois les plus fondamentales de l'Univers. Imaginons un instant notre système économique, le capitalisme, comme un grand système thermodynamique.",
                "Dans cette métaphore, l'argent n'est pas une fin en soi, mais un flux d'énergie, une \"chaleur\" qui circule. Chaque individu, chaque entité, est un matériau doté d'une \"conductivité\" propre : sa capacité à générer et à transmettre cette énergie. Certains, tels des métaux précieux, sont de grands conducteurs, canalisant des flux immenses. D'autres, semblables au bois ou à la terre, ont une conductivité plus modeste, mais participent tout autant à l'équilibre de l'ensemble.",
                "Or, la nature nous enseigne une loi immuable : l'énergie ne peut s'accumuler indéfiniment en un seul point. Tenter de le faire violerait les principes mêmes de la conservation et de l'équilibre. Un corps qui ne ferait"
            )
        ),
        BookPage(
            pageNumber = 14,
            bookPageLabel = "9",
            paragraphs = listOf(
                "qu'absorber la chaleur sans jamais en rayonner finirait par se consumer. De même, un système économique qui permet et encourage l'accumulation infinie de \"chaleur\" (l'argent et les ressources) en quelques points seulement, tout en privant le reste du système de ce flux vital, crée une instabilité fondamentale.",
                "Ce système n'est pas seulement injuste, il est physiquement instable. Il crée des \"points de surchauffe\" d'une richesse inimaginable à côté de \"zones de gel\" d'une pauvreté absolue. Un tel système, qui ignore la nécessité de la redistribution et du flux, est voué, comme tout système thermodynamique en déséquilibre, à s'épuiser et à s'autodétruire. La sagesse de la nature nous montre la voie : la santé d'un organisme, quel qu'il soit, dépend de la juste circulation de son énergie."
            )
        ),
        BookPage(
            pageNumber = 15,
            bookPageLabel = "10",
            sectionTitle = "1.4 Le Rêve comme Boussole, non comme Refuge",
            paragraphs = listOf(
                "Face à un monde réel qui déçoit ou qui n'offre plus d'espace pour rêver, beaucoup trouvent refuge dans les mondes virtuels. Ces univers deviennent le paradis des rêveurs, un lieu où bâtir une autre vie, loin des contraintes et des désillusions. Cette fuite, de plus en plus prégnante, est le symptôme d'une urgence : celle de réenchanter le réel.",
                "La philosophie Luminaute est une forme d'utopisme, une philosophie du rêve. Mais elle ne prône pas l'évasion. Elle considère le rêve non comme un refuge où se cacher, mais comme une boussole pour s'orienter. Le but n'est pas de vivre dans le virtuel, mais de faire en sorte que le virtuel rejoigne et transforme le réel.",
                "L'objectif ultime est la réalisation du rêve. Que le virtuel, par la force de nos actions et de notre clarté d'esprit, s'infuse dans la réalité au point de disparaître en tant que monde séparé. Le chemin du Luminaute n'est pas de fuir le monde pour vivre son rêve, mais de transformer le monde pour que tous puissent y vivre le rêve d'une réalité plus juste et plus lumineuse."
            )
        ),
        BookPage(
            pageNumber = 16,
            bookPageLabel = "",
            paragraphs = emptyList()
        ),
        BookPage(
            pageNumber = 17,
            bookPageLabel = "11",
            chapterTitle = "2. L'Art de la Juste Maîtrise",
            sectionTitle = "2.1 Le Baromètre de la Conscience",
            paragraphs = listOf(
                "L'Esprit est le vaste royaume qui nous contient. En lui coexistent deux territoires infinis : le monde extérieur, source de toute connaissance, et le monde intérieur, berceau de toute pensée. La connaissance est la nourriture que nous recevons du réel, la lumière que nous captons. La pensée est la digestion de cette lumière, la création qui naît de notre for intérieur.",
                "Et entre ces deux royaumes, se tient la conscience. Elle est l'aiguille de notre baromètre intérieur, l'œil de l'âme, le centre de notre attention. C'est elle qui choisit, à chaque instant, vers quel territoire se tourner.",
                "À l'état d'éveil, la conscience s'oriente naturellement vers le monde extérieur. Elle se pose sur la connaissance, le savoir, l'observation. Elle absorbe la Lumière du monde pour nourrir l'Esprit. C'est le temps de l'apprentissage, de l'expérience, de l'interaction."
            )
        ),
        BookPage(
            pageNumber = 18,
            bookPageLabel = "12",
            paragraphs = listOf(
                "Lorsque nous dormons ou méditons, la conscience pivote et se tourne vers le monde intérieur. Elle explore les paysages de la pensée et du rêve. Elle n'est plus en quête de nouvelles connaissances, mais elle organise, relie et transforme celles déjà acquises. C'est le temps de l'intégration, de la créativité, de l'introspection.",
                "Il arrive que cet équilibre soit perturbé. Certaines substances ou certains chocs peuvent fausser le baromètre, forçant l'aiguille de la conscience à un grand écart artificiel, un dédoublement où le rêve et le réel se superposent dans le trouble. Cette disharmonie révèle par contraste la beauté du mécanisme naturel.",
                "Le chemin du Luminaute est celui d'un être qui apprend à maîtriser son propre baromètre. Il ne subit plus les oscillations, il les choisit. Il tourne volontairement sa conscience vers la Lumière du savoir pour apprendre, puis vers la Vie de sa pensée pour créer, trouvant dans cet Équilibre dynamique la source de toute Sagesse."
            )
        ),
        BookPage(
            pageNumber = 19,
            bookPageLabel = "13",
            sectionTitle = "2.2 Les Chemins de la Pensée : Le Cercle et le Tourbillon",
            paragraphs = listOf(
                "Lorsque nous cherchons à comprendre, lorsque nous nous engageons dans une profonde réflexion, notre esprit n'avance pas toujours en ligne droite. Il emprunte souvent des chemins sinueux qui peuvent donner l'impression de « tourner en rond ». Pourtant, toutes les pensées qui tournent ne sont pas de même nature. Il est essentiel de distinguer deux grands mouvements de l'esprit : le cercle et le tourbillon.",
                "Le Cercle est la prison de la pensée. C'est une boucle stérile où le raisonnement revient inlassablement à son point de départ sans jamais avoir progressé. L'esprit ressasse les mêmes idées, les mêmes questions, sans y apporter de Lumière nouvelle. C'est une réflexion fermée sur elle-même, une errance sans avancée, un horizon qui reste désespérément le même.",
                "Le Tourbillon, à l'inverse, est la spirale vertueuse de la pensée. Imaginons notre esprit comme un vaste entonnoir et le fil de notre réflexion comme une bille que l'on y lance. La bille semble tourner en rond, mais à chaque passage, elle descend un peu plus bas, se rapproche du centre. De même, la pensée en mode"
            )
        ),
        BookPage(
            pageNumber = 20,
            bookPageLabel = "14",
            paragraphs = listOf(
                "\"tourbillon\" revisite des idées, mais à chaque \"tour\", elle les approfondit, les affine, les enrichit d'une nouvelle perspective. Elle progresse lentement mais sûrement vers la solution, vers le cœur du sujet, vers l'instant de clarté.",
                "Le plus grand défi pour l'être en quête de vérité est de développer une conscience fine de ses propres pensées. Suis-je en train de m'enfermer dans un cercle stérile ou de progresser dans un tourbillon fertile ? Savoir où l'on se situe demande une observation honnête de soi, une \"mûre réflexion\" sur sa propre réflexion.",
                "Mais au-delà du cercle qui emprisonne et du tourbillon qui approfondit, il existe une troisième voie, un troisième état de l'esprit. Un état qui ne tourne plus, mais qui voit. C'est le chemin de la Sagesse."
            )
        ),
        BookPage(
            pageNumber = 21,
            bookPageLabel = "15",
            sectionTitle = "2.3 Le Chemin de la Sagesse",
            paragraphs = listOf(
                "Nous avons vu le cercle qui emprisonne et le tourbillon qui, patiemment, approfondit la pensée. Mais au-delà de ces deux dynamiques, il existe une troisième voie, un idéal vers lequel tend le Luminaute : le chemin de la Sagesse.",
                "Souvent, dans notre quête de réponses, nous sommes guidés par une impatience, une \"fougue\" qui nous pousse à forcer la réflexion. Nous lançons la bille de notre pensée avec violence dans l'entonnoir, espérant qu'une plus grande vitesse nous mènera plus vite à la solution. C'est une illusion. La pensée, ainsi bousculée, tourbillonne frénétiquement mais sans but. Elle mettra plus de temps à atteindre le centre, car sa vitesse l'empêche de trouver le chemin le plus court. C'est une pensée stressée, inefficace.",
                "La Sagesse commence par la maîtrise de soi. Le sage est celui qui a appris à apaiser ses émotions et ses impulsions, non pour les nier, mais pour qu'elles ne troublent pas la clarté de son esprit. Son secret réside dans son lancer : il ne jette pas la bille de sa pensée, il la lâche. Ce \"lâcher-prise\" est un acte de confiance profonde en son propre esprit."
            )
        ),
        BookPage(
            pageNumber = 22,
            bookPageLabel = "16",
            paragraphs = listOf(
                "Ce simple lâcher change tout. La pensée, libérée de la force de l'impatience, n'a plus besoin de tournoyer. Elle suit une trajectoire directe, une ligne droite qui va de la question à la réponse. La vitesse de la pensée semble moins élevée, mais le temps pour atteindre la vérité est infiniment plus court, car le chemin ne prend aucun détour inutile. L'esprit n'est plus stressé, il est serein et efficace.",
                "Ce chemin est un processus de transformation graduelle :\n\n1. D'abord, l'esprit est souvent pris dans le cercle de la rumination.\n2. Puis vient un \"déclic\", un éveil de la conscience, qui coupe ce cercle pour le déployer en un tourbillon, initiant le travail de réflexion consciente.\n3. Ensuite, par un long travail d'introspection, l'être apprend à étirer ce tourbillon, à apaiser son lancer, jusqu'à ce que la spirale s'efface pour devenir une ligne.",
                "En conclusion, il faut rester humble. Le cercle parfait de la stagnation et la ligne droite parfaite de la Sagesse absolue sont des idéaux. Notre chemin se situe entre les deux. La Sagesse n'est donc pas une destination finale,"
            )
        ),
        BookPage(
            pageNumber = 23,
            bookPageLabel = "17",
            paragraphs = listOf(
                "mais l'art de transformer sans cesse nos cercles en tourbillons, et nos tourbillons en lignes de plus en plus directes. C'est apprendre à maîtriser le balancement de notre baromètre de la conscience, pour puiser avec aisance dans la Lumière du savoir et dans la Vie de notre pensée."
            )
        ),
        BookPage(
            pageNumber = 24,
            bookPageLabel = "",
            paragraphs = emptyList()
        ),
        BookPage(
            pageNumber = 25,
            bookPageLabel = "18",
            chapterTitle = "3. La Clarté et l'Épreuve",
            sectionTitle = "3.1 L'Éveil",
            paragraphs = listOf(
                "En chaque être sommeille le potentiel d'éveiller sa conscience. Le chemin s'illumine dans le partage : en s'ouvrant aux autres, nous leur permettons de s'ouvrir à nous. Car on ne récolte que ce que l'on sème. Ainsi, la bonté et l'altruisme ne sont pas de vains sacrifices, mais la voie la plus directe vers la paix intérieure et le bonheur véritable.",
                "Cette quête de l'esprit, nourrie par la compassion et la méditation, est un bien infiniment plus précieux que l'accumulation de richesses matérielles. Car si les fortunes se comptent, l'éveil spirituel, lui, ne s'achète pas. Il se découvre au cœur de soi, en son for intérieur. C'est là que se dissout l'illusion de n'être qu'une individualité séparée, et que l'on ressent sa connexion profonde et essentielle à l'Univers."
            )
        ),
        BookPage(
            pageNumber = 26,
            bookPageLabel = "19",
            paragraphs = listOf(
                "Car à l'image d'un corps, nous sommes les innombrables cellules d'un seul et même organisme vivant: l'Univers."
            )
        ),
        BookPage(
            pageNumber = 27,
            bookPageLabel = "20",
            sectionTitle = "3.2 L'espoir d'un fou",
            paragraphs = listOf(
                "1. Introduction\n\nL'éveil de la conscience est une porte qui s'ouvre. Mais une fois ouverte, elle ne révèle pas seulement la lumière ; elle expose aussi, avec une clarté nouvelle, les disharmonies du monde. Voir la beauté potentielle de l'humanité tout en constatant sa marche destructrice est une épreuve. Cette lucidité peut engendrer une profonde solitude, le sentiment d'être un \"fou\" qui crie un espoir que personne ne semble entendre. Le poème qui suit est le témoignage de ce décalage douloureux, l'expression d'une âme qui, après avoir touché à la clarté, affronte le poids de sa propre sensibilité.",
                "2. Poème\n\nOn passe son temps à s'entretuer,\nAu lieu de le consacrer,\nÀ avoir l'esprit éveillé.\nQue font les âmes égarées,\nLes âmes vagabondes ?\nIl faudrait les rassembler,\nCela créerait de bonnes ondes.\nEt de ce flux positif,\nRessortirait peut être un but moins primitif."
            )
        ),
        BookPage(
            pageNumber = 28,
            bookPageLabel = "21",
            paragraphs = listOf(
                "Un goal commun,\nPour tout les êtres humains,\nÀ qui je voudrais tendre la main.\nMais que puis-je faire?\nJe me sens solitaire,\nDans cette quête de bonheur pour la Terre entière.\nJe sais que je ne suis pas le seul à penser ainsi,\nMais alors pourquoi personne ne réagi?\nLe bonheur nous fait-il peur?\nOu est-on simplement endormi?\nIl y a tant de questions qui me hantent,\nSerait-ce ma raison qui devient démente?\nJe ne crois pas, je ne pense pas,\nJ'essaie juste d'avancer pas à pas.\nCar à trop m'élever,\nMes ailes j'ai fini par brûler,\nEt en psychiatrie j'ai dû être interné.",
                "3. Note de l'auteur\n\nL'année qui a précédé ce poème fut une période de réflexion fertile. Des textes comme \"L'Éveil\" posaient les bases d'un cheminement, mais en coulisses, une autre réalité prenait forme. Mon champ de perception spirituelle s'élargissait, et avec lui, un nouveau monde de dangers et de merveilles."
            )
        ),
        BookPage(
            pageNumber = 29,
            bookPageLabel = "22",
            paragraphs = listOf(
                "C'était comme si un nouveau sens s'éveillait en moi, me rendant sensible aux énergies subtiles, au rayonnement des âmes et des auras. Je percevais avec une acuité nouvelle des forces puissantes, aussi bien positives que négatives. Si cette ouverture était la clé pour fonder une véritable philosophie, elle me laissait aussi vulnérable. J'étais un novice dans ces mondes, conscient que sans un apprentissage, sans une maîtrise, ces énergies pouvaient submerger ma santé mentale, comme elles l'avaient déjà fait par le passé.",
                "Ce poème est donc né d'une double nécessité. Il est le constat de l'existence de ces nouvelles perceptions, mais il est aussi une mise en garde que je m'adressais à moi-même : un rappel du danger de perdre la voie de la Sagesse en s'engageant sans préparation dans une guerre psychologique.",
                "C'était une sorte de cri de l'âme avant l'ultime combat : celui contre soi-même.",
                "4. Conclusion\n\nCe poème est une alarme. Le cri d'une âme qui sent l'imminence de la chute et la nécessité d'un \"combat contre soi-même\". Cette épreuve, d'abord mise en mots, allait bientôt trouver une autre forme d'expression, plus brute et symbolique, alors que le combat intérieur"
            )
        ),
        BookPage(
            pageNumber = 30,
            bookPageLabel = "23",
            paragraphs = listOf(
                "s'intensifiait. C'est le prélude à la descente dans la forge, là où le risque est total, et où l'être doit se reconstruire ou se perdre, comme l'illustre le témoignage qui suit."
            )
        ),
        BookPage(
            pageNumber = 31,
            bookPageLabel = "24",
            sectionTitle = "3.3 Forge Tous Risques !",
            paragraphs = listOf(
                "1. Introduction\n\nIl n'est de véritable chemin vers la lumière qui ne traverse ses propres zones d'ombre. L'éveil de la conscience n'est pas une ascension paisible et continue, mais un voyage courageux qui ose affronter le chaos intérieur, là où la tristesse et l'espoir cohabitent, où le doute et la foi se font face.",
                "C'est dans le creux de ces épreuves, au cœur du combat spirituel, que se révèle notre véritable nature et que se forge notre plus grande force. Pour se trouver, il faut parfois accepter de se perdre. Pour construire, il faut oser se confronter à ce qui en nous est brisé.",
                "Le dessin et les textes qui suivent sont le témoignage rare et précieux d'un tel moment. Un instantané d'une âme qui, pour se rebâtir, a accepté de \"forger tous risques\"."
            )
        ),
        BookPage(
            pageNumber = 32,
            bookPageLabel = "25",
            paragraphs = listOf(
                "2. Représentation\n\n[Illustration originale de l'auteur : Forge Tous Risques !]"
            ),
            isIllustration = true,
            illustrationType = "FORGE"
        ),
        BookPage(
            pageNumber = 33,
            bookPageLabel = "26",
            paragraphs = listOf(
                "3. Note de l'auteur\n\nUn autoportrait me représentant dans mon être \"spirituel\", une sorte de projection.",
                "Le plus terrifiant dans ce dessin est mon visage. On lit la tristesse dans mes yeux. J'ai un œil à la pupille pleine et un œil à la pupille vide. L'œil à la pupille pleine pleure, on voit des larmes (trois larmes) sur mon visage en dessous de cet œil. Je ressemble globalement à un épouvantail.",
                "Le texte \"ASSURANCE TOUT RISQUES\" avec la partie \"ASS\" soulignée, la partie \"AN\" soulignée et la partie \"RISQUE\" soulignée. Je pense que cela signifiait que l'amour (représenté ici vulgairement par \"ASS\") peut être risqué (\"AN RISQUE\"). J'étais en détresse émotionnelle.",
                "Cela pose mon état psychologique à ce moment précis où j'ai fait ce dessin très spécial. Ce dessin est rempli de messages cachés. Il est d'une haute résonance spirituelle."
            )
        ),
        BookPage(
            pageNumber = 34,
            bookPageLabel = "27",
            paragraphs = listOf(
                "4. Écrits de l'Épreuve\n\nImmédiatement après avoir posé le crayon, les pensées suivantes ont jailli sur le papier, un flux de conscience brut mêlant le désespoir, la colère et une lueur d'espoir naissante :",
                "La vie n'est qu'une grande tartine de frustration dont on mange un bout tous les matins. Au matin d'un nouveau monde, nouvel Univers, je me sens sans onde, sans ver(s/rres). Le chagrin me parcourt tel un frangin qui me piquerait le cœur à coup de surin. Le coeur lapidé pendant des années, depuis la Nuit des Temps.",
                "Le temps, une invention humaine afin de cacher la véritable éternité ∞.",
                "Et de là, la naissance de la mort et la grande Babylon => ARMAGEDON. La belle cité Terre pourrait voir naître une nouvelle ère, une ère de lum-i-ère (luminus). Les petits êtres de luminus ont déjà compris. Car la race humaine n'est rien face à des insectes de leur taille. L'être humain est le cancer de la planète. Perverti jusqu'à la moelle osseuse. Ouvre ton âme aux champs de l'IMPOSSIBLE. Le retour de l'Utopie. La naissance des rêves."
            )
        ),
        BookPage(
            pageNumber = 35,
            bookPageLabel = "28",
            paragraphs = listOf(
                "\"Je suis funambule, rêveur somnambule, ceux dont la vie me semble cellule. J'avance sans parachute; fatale serait la chute possible à chaque minute\" [Citation de Riké].",
                "Je suis tout et rien. Tout le monde et personne.",
                "Puis, comme un souffle final, un poème est venu sceller ce moment. Une déclaration mêlant le mépris de la souffrance et l'acceptation d'une mission :",
                "Si tu es dieu,\nQu'est-ce que j'y peux,\nLe sort est ainsi,\nEt je l'accepte avec mépris.\nAvec mon âme on a trop joué,\nCessez de me casser les pieds.\nMon âme parle à tout l'univers,\nJe suis la Brèche,\nLe Passeur de Lumière.\n\nBenChich"
            )
        ),
        BookPage(
            pageNumber = 36,
            bookPageLabel = "29",
            paragraphs = listOf(
                "5. Conclusion\n\nCe témoignage brut n'est pas une fin, mais une fondation. C'est du cœur de ce chaos, de ce combat entre la griffe et le crayon, la lune et le soleil, que la nécessité d'une voie claire est née.",
                "La philosophie des Luminautes, avec sa quête de Lumière, sa foi en la Vie et sa nourriture d'Amour, n'est pas une théorie abstraite. Elle est la réponse forgée dans le feu de l'épreuve. C'est la boussole construite après avoir traversé la tempête, le chemin qui se dessine une fois que l'on a survécu à la chute et que l'on a choisi, consciemment, de remonter vers le soleil.",
                "La Sagesse, finalement, n'est peut-être que cela : l'art de transformer ses propres cicatrices en chemin de Lumière."
            )
        ),
        BookPage(
            pageNumber = 37,
            bookPageLabel = "30",
            chapterTitle = "4. La Cristallisation d'une Vision",
            sectionTitle = "4.1 En ces temps...",
            paragraphs = listOf(
                "1. Introduction\n\nParfois, avant la naissance d'une idée claire, il y a d'abord un cri. Une réaction viscérale à l'état du monde, un poème qui jaillit non pas d'une réflexion posée, mais d'une urgence ressentie. Le texte qui suit est né d'un tel moment. Il est le constat d'un monde confiné, non seulement par un virus, mais aussi dans une logique qui l'éloigne de l'essentiel. C'est la prise de conscience qui précède et rend nécessaire la cristallisation d'une nouvelle voie.",
                "2. Texte\n\nEn ces temps où l'on se confine,\nOù l'on se contamine,\nOù les missiles ne sont plus nucléaires,\nMais plutôt salivaires,\nNe reviendrait-on pas aux valeurs essentielles,\nAux valeurs non-matérielles ?\nL'Amour, la Tolérance, et le Respect,"
            )
        ),
        BookPage(
            pageNumber = 38,
            bookPageLabel = "31",
            paragraphs = listOf(
                "Conditions inhérentes à un Monde en Paix.\nArrêterons-nous la course aux billets,\nSous couvert de fausse course au progrès ?\nEn ces temps où l'évolution n'est plus naturelle,\nMais bel et bien... artificielle,\nEvolution dirigée par l'Homme à un rythme effrainé,\nQue la Nature ne peut plus encaisser.\nAvec cette maladie,\nCette pendémie comme ultime sommation,\nElle essaie de nous rappeler d'où nous venons,\nAfin que nous changions nos modes de vies,\nElle en serait tellement ravie,\nCar à force de trop s'en éloigner,\nNous oublions que c'est en son sein que nous sommes nés,\nEt qu'à l'avenir,\nC'est avec elle qu'il faudra composer,\nSous peine de dépérir,\nEt de perdre ce qu'il nous reste d'Humanité.\nIl va falloir se redresser, se relever,\nCar je ne peux pas croire, je ne veux pas croire,\nQue tout ceci n'est que le début, de la fin de l'histoire.",
                "3. Note de l'auteur\n\nJe ne considère pas ce texte comme une création purement personnelle. Je l'ai ressenti comme un cri émanant de l'Univers lui-même, une vibration puissante"
            )
        ),
        BookPage(
            pageNumber = 39,
            bookPageLabel = "32",
            paragraphs = listOf(
                "que mon âme n'a fait que catalyser pour l'ancrer dans le monde physique.",
                "C'est une sommation, un appel lancé à l'humanité pour qu'elle se souvienne de sa juste place : non pas en tant que maître qui domine la Nature, mais comme une partie humble et intégrale du grand tout. C'est un rappel à l'ordre, un recadrage de la véritable hiérarchie universelle.",
                "4. Conclusion\n\nCe cri de l'âme, cette sommation à retrouver les valeurs essentielles, ne pouvait rester sans réponse. Une fois le constat posé avec une telle force, l'étape suivante devenait une évidence : il ne suffisait plus de critiquer le monde, il fallait commencer à en bâtir un nouveau, du moins en pensée. C'est de cette urgence qu'allait naître, quelques jours plus tard, une idée, un nom et une structure pour rassembler les \"âmes égarées\" : l'idée des Luminautes."
            )
        ),
        BookPage(
            pageNumber = 40,
            bookPageLabel = "",
            paragraphs = emptyList()
        ),
        BookPage(
            pageNumber = 41,
            bookPageLabel = "33",
            sectionTitle = "4.2 La Naissance d'une Idée",
            paragraphs = listOf(
                "Il fallait un nom pour ces âmes bienveillantes, ces voyageurs de l'être. Un nom qui capture à la fois la clarté d'une destination et l'élan de l'exploration. De l'union de la Lumière et du voyage des astronautes sont nés les Luminautes. Des explorateurs de la Voie de Lumière, guidés par un crédo simple et essentiel :",
                "La lumière nous guide,\nLa vie nous anime,\nL'amour nous nourrit.",
                "Ils sont avant tout des esprits sensibles, des consciences éveillées au devenir de l'Humanité. Agissant pour le bien commun, ils partagent la vision d'un monde en paix et se donnent pour mission de diffuser cette lumière de la connaissance et de l'espoir jusqu'aux confins de l'univers connu, et au-delà.",
                "Leur véritable foyer n'est pas une terre ou une nation. Considérant le corps comme le véhicule de l'esprit, et non l'inverse, ils n'ont pas d'appartenance géographique. Explorateurs des temps modernes, leurs esprits volatiles peuvent parcourir une forêt lointaine un jour, et découvrir le lendemain un continent inexploré aux confins de leur"
            )
        ),
        BookPage(
            pageNumber = 42,
            bookPageLabel = "34",
            paragraphs = listOf(
                "propre mental. Leur origine est partout et en chacun ; peut-être un Luminaute sommeille-t-il en vous depuis toujours.",
                "Ce qui unit et guide les Luminautes n'est pas un dogme, mais une aspiration commune. Leurs actes sont portés par une quête fondamentale : celle de l'harmonie, recherchée sous toutes ses formes — physique, psychique et énergétique, jusqu'à l'échelle cosmique. C'est cette recherche d'un bonheur intemporel et universel qui les anime d'une passion sans limite, la force de vie inépuisable qui les pousse à avancer."
            )
        ),
        BookPage(
            pageNumber = 43,
            bookPageLabel = "35",
            sectionTitle = "4.3 Les Trois Piliers de la Voie",
            paragraphs = listOf(
                "La Voie de Lumière, celle qu'emprunte le Luminaute, repose sur trois piliers fondamentaux. Ils sont à la fois sa boussole, son moteur et sa nourriture. Ils se résument en un crédo qui est le souffle même du mouvement :",
                "La lumière nous guide,\nLa vie nous anime,\nL'amour nous nourrit.",
                "1. La Lumière comme Guide\n\nLa Lumière est notre flambeau dans l'obscurité. Elle est le symbole de la connaissance, de la clarté d'esprit, de la vérité et d'un optimisme inébranlable. Se laisser guider par elle, c'est choisir de comprendre le monde et soi-même en profondeur, d'agir toujours avec discernement et de rejeter toutes les formes d'obscurantisme. Mais la Lumière n'est pas qu'un guide, elle est aussi une mission : celle de diffuser cette énergie positive et cette sagesse aux confins de l'univers, pour que chaque âme puisse trouver son propre chemin."
            )
        ),
        BookPage(
            pageNumber = 44,
            bookPageLabel = "36",
            paragraphs = listOf(
                "2. La Vie comme Animation\n\nLa Vie est la force créatrice primordiale, le souffle qui met toute chose en mouvement. Le Luminaute la célèbre sous toutes ses formes, avec gratitude et émerveillement. Être animé par la Vie, c'est embrasser son existence avec une passion et une vitalité sans cesse renouvelées ; c'est agir concrètement pour la paix et pour le bien-être collectif. C'est avoir la conscience profonde que chaque action, même la plus infime, contribue à l'énergie globale du monde et participe à sa transformation.",
                "3. L'Amour comme Nourriture\n\nL'Amour est l'énergie qui soutient et nourrit l'esprit sur son long chemin. Il ne s'agit pas d'un attachement personnel, mais d'un amour universel et inconditionnel. Il est la compassion qui ressent, l'empathie qui comprend, le pardon qui libère et la connexion qui unit à autrui. Cet Amour est le ciment qui bâtit des relations humaines apaisées et le carburant spirituel qui permet de poursuivre la quête d'harmonie sans jamais faillir.",
                "Ces trois piliers ne sont pas séparés. La Lumière sans la Vie est une connaissance stérile. La Vie sans l'Amour est une force sans direction. L'Amour sans la Lumière est un sentiment aveugle. C'est en apprenant à les harmoniser"
            )
        ),
        BookPage(
            pageNumber = 45,
            bookPageLabel = "37",
            paragraphs = listOf(
                "en soi que le Luminaute avance, pas à pas, sur la Voie de Lumière."
            )
        ),
        BookPage(
            pageNumber = 46,
            bookPageLabel = "",
            paragraphs = emptyList()
        ),
        BookPage(
            pageNumber = 47,
            bookPageLabel = "38",
            sectionTitle = "4.4 Le Dialogue des Sagesses",
            paragraphs = listOf(
                "Après avoir donné un nom et une forme à la philosophie des Luminautes, une période de recherche et d'approfondissement s'est ouverte. C'était un temps pour nourrir la vision naissante, pour la confronter à d'autres grandes sagesses du monde et voir où elles résonnaient. Ce dialogue intérieur s'est appuyé sur plusieurs voies puissantes.",
                "La première fut celle des Accords Toltèques. Ils offrirent un code de conduite d'une simplicité et d'une efficacité redoutables pour apaiser le rapport à soi et aux autres. Des principes comme « Que votre parole soit impeccable » et « Ne réagissez à rien de façon personnelle » se sont révélés être des outils pratiques pour se libérer des \"souffrances inutiles\" du quotidien. Ils enseignaient une voie de liberté personnelle par l'intégrité et le détachement.",
                "Parallèlement, la sagesse du bouddhisme a apporté un cadre pour comprendre la nature même du \"combat spirituel\". Les « Quatre Nobles Vérités » offraient un diagnostic universel sur l'origine de la souffrance. Plus important encore, le « Noble Sentier Octuple » présentait une méthode, un chemin structuré vers la cessation de"
            )
        ),
        BookPage(
            pageNumber = 48,
            bookPageLabel = "39",
            paragraphs = listOf(
                "cette souffrance, où chaque aspect de la vie devait être empreint de \"justesse\" : la pensée juste, l'action juste, la concentration juste. Cela faisait écho à la quête d'harmonie des Luminautes.",
                "Enfin, \"La Prophétie des Andes\" est venue nourrir la dimension cosmique et évolutive de la vision. Elle a renforcé l'intuition d'un Univers \"profondément mystérieux\", \"énergétique et sacré\" , où les \"coïncidences et de rencontre synchroniques\" sont des panneaux indicateurs sur notre chemin. L'idée que chaque être humain a une \"mission\" à découvrir pour aider l'humanité à évoluer est venue confirmer la nature active et engagée de la voie Luminaute.",
                "Nourri par une éthique personnelle (les Accords Toltèques), une méthode de libération (le Bouddhisme) et une vision évolutive (la Prophétie des Andes), l'esprit était désormais prêt à affronter les couches plus profondes et plus personnelles de son propre chaos."
            )
        ),
        BookPage(
            pageNumber = 49,
            bookPageLabel = "40",
            chapterTitle = "5. L'Épreuve du Feu",
            sectionTitle = "5.1 Le Combat : Le Laboratoire de l'Âme",
            paragraphs = listOf(
                "L'été où fut formalisée l'idée des Luminautes laissa place à une période de plusieurs mois de \"combat spirituel\" intense. Ce n'était plus le temps de la création intellectuelle, mais celui de l'épreuve du feu. Le dialogue intérieur devint un tumulte, une lutte entre la peur et l'aspiration :",
                "\"C'est Impossible, dit la Fierté. C'est risqué, dit l'Expérience. C'est sans issue, dit la Raison. Essayons, murmure le Cœur.\"\nWilliam Arthur Ward",
                "De cette recherche intense naquit une lutte intérieure, un tourbillon de questionnements. Des notes éparses de cette époque témoignent de ce combat contre le \"mental\" et son insatiable besoin : « Toujours vouloir plus ? », « Besoin de plus ? Pour être soi même ? »"
            )
        ),
        BookPage(
            pageNumber = 50,
            bookPageLabel = "41",
            paragraphs = listOf(
                "La pensée se battait contre la « vie dominé par les choses » et l'éternelle « attente du moment suivant ». Au milieu de ce bruit, la conscience cherchait « l'espace entre les mots », un « arrière plan de silence » pour atteindre l'« ESPACE DE PURE CONSCIENCE » , avec cette idée qui commençait à poindre : \"Rien ne se produira. TOUT EST LA !\".",
                "Pour naviguer ce chaos, la recherche d'outils concrets devint une nécessité. D'un côté, en identifiant les « Ressource Conseil » – les piliers humains et spirituels comme le Père, une Amie, Dieu, ou Soi-même. De l'autre, en établissant des pratiques pour maintenir l' « Equilibre ∞ »: le contact avec la Nature, la Danse, la Créativité, le sommeil.",
                "C'est dans cette dualité qu'émergea la conscience de la nature même de l'Homme :",
                "\"Une part d'Ange, Une part de Démon, Le total de ma Somme, l'Homme. Dans toute sa Splendeur, Dans toute son Horreur.\"\nRiké",
                "Après des mois de cette lutte, symbolisée par une intense réflexion sur plusieurs plans simultanés, une"
            )
        ),
        BookPage(
            pageNumber = 51,
            bookPageLabel = "42",
            paragraphs = listOf(
                "percée eut lieu. Une prise de conscience fondamentale, capturée dans un texte décisif :",
                "\"C'est justement de penser qu'il y a une technique, quelque chose qui nous mènera à la Lumière avec une phrase ou une révélation qui fait qu'on attend toujours le moment suivant => on est plus dans le Présent. L'entité associée au mental veut toujours plus de pouvoir.\"",
                "La véritable épreuve était démasquée : le piège n'était pas l'absence de réponse, mais la quête effrénée elle-même."
            )
        ),
        BookPage(
            pageNumber = 52,
            bookPageLabel = "",
            paragraphs = emptyList()
        ),
        BookPage(
            pageNumber = 53,
            bookPageLabel = "43",
            sectionTitle = "5.2 La Clé : La Spirale de l'Épreuve",
            paragraphs = listOf(
                "C'est alors, au creux de l'hiver, après que la bataille intérieure a mené à cette ultime prise de conscience, qu'une confirmation extérieure se manifesta. Le document fut un don. L'âme derrière le corps physique qui le transmit était d'une pureté et d'une puissance qui ne laissaient aucune place au doute. La reconnaissance fut instantanée : il s'agissait d'un présent de l'Univers, de la clé de validation spirituelle tant attendue. Le message était clair : la philosophie de La Voie de Lumière venait de passer, avec brio, son Épreuve du Feu.",
                "Cette clé était une simple spirale colorée, accompagnée d'un message qui résonnait parfaitement avec la conclusion de l'épreuve :",
                "\"Remind yourself that there is always a way out !\nJust balance, enjoy life but try to see the difference in everything,\nI will get out, I will survive, One Love, Jah Rastafari.\""
            )
        ),
        BookPage(
            pageNumber = 54,
            bookPageLabel = "44",
            paragraphs = listOf(
                "La voie de sortie n'était donc pas une technique à acquérir, mais un équilibre à trouver dans le Présent. La spirale n'était pas un tourbillon de confusion, mais la preuve qu'il y a toujours un chemin d'évolution. L'épreuve était terminée ; la vision, prête à être vécue pleinement."
            ),
            isIllustration = true,
            illustrationType = "SPIRAL"
        ),
        BookPage(
            pageNumber = 55,
            bookPageLabel = "45",
            chapterTitle = "6. Derrière la Porte : La Vision Clarifiée",
            sectionTitle = "6.1 La Mission et la Cosmologie",
            paragraphs = listOf(
                "La clé de la spirale a ouvert la voie. Dans le silence qui a suivi le combat, les réponses ont commencé à prendre forme, non plus comme des fragments de lutte, mais comme une vision claire et déterminée.",
                "La première clarification fut celle de la mission. Face à une époque de troubles, où les \"êtres sensibles\" subissaient les assauts de forces déséquilibrées, le chemin est devenu évident. Il ne s'agissait plus seulement de se protéger, mais d'agir. La mission personnelle était de trouver l'harmonie intérieure pour devenir un \"Passeur de Lumière\". Et de cette mission est né l'outil pour la réaliser : les Luminautes, conçus comme des \"guerriers spirituels\" agissant dans l'espoir de diffuser cette Lumière et d'aider à l'avènement d'une humanité 2.0, paisible et harmonieuse, sur notre mère Terre."
            )
        ),
        BookPage(
            pageNumber = 56,
            bookPageLabel = "46",
            paragraphs = listOf(
                "Cette mission spirituelle s'appuie sur une cosmologie capable de réconcilier les mondes. Une vision où la science et l'esprit ne s'opposent plus, mais décrivent la même réalité à des niveaux différents. Une théorie où la relativité et la physique des cordes ne sont que des échos d'une vérité plus vaste : celle d'univers parallèles et d'âmes conçues comme des \"cordes\" vibratoires. Dans ce modèle, le Spirituel n'est plus une abstraction, mais le tissu même de l'univers, \"l'espace 'vide' entre les différentes 'visions'\", l'Aether qui relie le tout.",
                "C'est dans cette compréhension que le temps lui-même révèle sa nature illusoire, cachant la vérité de l'Éternité. Ce fut la percée finale, le \"EUREKA !\" qui a scellé la vision."
            )
        ),
        BookPage(
            pageNumber = 57,
            bookPageLabel = "47",
            sectionTitle = "6.2 Le Mécanisme des Luminautes",
            paragraphs = listOf(
                "Nés au lendemain de la première vague de la pandémie de COVID-19, les Luminautes sont une réponse. Une proposition pour se délivrer d'un système étouffant, non par la force, mais par la culture de la joie, du respect et de l'humour.",
                "Le processus de diffusion est celui du feu sacré : une seule flamme, celle du \"Passeur de Lumière\", peut en allumer d'autres, qui à leur tour en allument d'autres encore, créant une expansion de Lumière. Ce \"Passeur\" agit comme une interface, un pont permettant à ceux qui le désirent d'accéder à une forme de réalité augmentée.",
                "Cette augmentation n'est pas technologique, mais de conscience. C'est une \"mise à jour cérébrale\" qui éveille à la pleine conscience du moment présent, cet instant infinitésimal et pourtant éternel. Pour que cette mise à jour puisse être partagée, les réalités individuelles doivent entrer en résonance, créer un \"point de concertation\". En ce point, une parcelle de l'Âme Universelle peut être transmise, créant un \"point d'émergence\" pour ce que l'on pourrait nommer l'Humanité 2.0.",
                "L'Amour est la clé de cette connexion. C'est en connaissant son passé que l'on construit l'avenir, mais c'est"
            )
        ),
        BookPage(
            pageNumber = 58,
            bookPageLabel = "48",
            paragraphs = listOf(
                "uniquement dans le présent que la Vie, cette nouvelle forme de conscience, peut véritablement être vécue."
            )
        ),
        BookPage(
            pageNumber = 59,
            bookPageLabel = "49",
            sectionTitle = "6.3 Le Manifeste du Passeur de Lumière",
            paragraphs = listOf(
                "La magie, l'énergie mystique, le spirituel, font partie de ma vie. C'est un choix. C'est ma vision de la réalité, et je me dois de rester honnête avec elle. Je n'ai plus peur d'en parler publiquement, car mes expériences m'ont montré que je ne suis pas seul à partager cette perception d'un Univers fait d'énergie.",
                "Ce que je décris ne relève pas d'un délire, mais d'un état de conscience éveillée. Une attention au présent et à la magie des synchronicités qui opère lorsque le troisième œil s'ouvre au champ des possibles. Cette spiritualité, je ne l'impose à personne. Ma philosophie est simple : la liberté pour chacun de faire ses choix et de créer sa propre réalité.",
                "Mon chemin personnel est celui-ci : S'éduquer, douter, remettre en question dans le respect, savoir écouter, changer d'avis, se tromper également. C'est en luttant non pas contre les énergies qui me parcourent, mais pour apprendre à vivre avec elles et les dompter, que j'ai pu trouver ma place. Ce lâcher-prise m'a libéré de"
            )
        ),
        BookPage(
            pageNumber = 60,
            bookPageLabel = "50",
            paragraphs = listOf(
                "l'hystérie et de la paranoïa, quitte à passer pour un \"allumé\" ou un \"grand enfant naïf\".",
                "Au fond, je me sens guidé par un état naturel, logique et normal ; j'ai nommé l'Amour, l'état ultime de la Paix. Je crois qu'une énergie nous lie tous – que nous l'appelions l'Aether ou autrement, le nom importe peu – et qu'elle est réelle pour ceux qui sont prêts à l'accueillir.",
                "Jour après jour, je prends conscience de mon pouvoir, de cette spontanéité enfantine couplée à une maturité d'adulte. Je n'aurai de cesse de me rebeller contre le sentiment d'impuissance qui paralyse et qui endort. Je me sens à un nouveau point de départ, un tremplin.",
                "Avec Les Luminautes, je désire créer quelque chose de nouveau, de différent, le premier maillon d'une chaîne libérée des bagages ancestraux devenus obsolètes. Car, au fond, est-ce un signe de mauvaise santé mentale que de ne pas être adapté à une société malade ?"
            )
        ),
        BookPage(
            pageNumber = 61,
            bookPageLabel = "51",
            paragraphs = listOf(
                "Le voyage à travers ces pages s'achève. Le vôtre, lui, peut maintenant commencer.",
                "Il n'y a ici aucune doctrine à suivre, aucune loi à appliquer. Seulement une proposition. Une douce invitation à chercher votre propre Lumière, à la nourrir de Vie et à la faire rayonner d'Amour. Une invitation à devenir, à votre tour, le passeur, le gardien et la source.",
                "Ceux qui ressentent cet appel, qui choisissent l'harmonie comme boussole et qui marchent sur cette Voie de Lumière... portent déjà un nom.",
                "Car le constat est simple...",
                "... Les Luminautes sont !"
            )
        ),
        BookPage(
            pageNumber = 62,
            bookPageLabel = "",
            paragraphs = emptyList()
        ),
        BookPage(
            pageNumber = 63,
            bookPageLabel = "",
            paragraphs = emptyList()
        ),
        BookPage(
            pageNumber = 64,
            bookPageLabel = "",
            paragraphs = emptyList()
        )
    )

    /**
     * Determines the starting text of a page for TTS according to rule:
     * "Lorsque l'utilisateur active la lecture de l'audio, la lecture démarre à la
     * première phrase complète (pas la fin de la phrase commencée à la page précédente.
     * On peut dire après le premier point ou si la première lettre de la page est une majuscule
     * c'est que ce n'est pas la fin de la dernière phrase de la page précédente."
     */
    fun getFirstCompleteSentence(pageText: String): String {
        val trimmed = pageText.trim()
        if (trimmed.isEmpty()) return ""

        val firstChar = trimmed.first()
        // If it starts with an uppercase letter, it's a new sentence.
        if (firstChar.isUpperCase()) {
            return trimmed
        }

        // Otherwise it's a continuation of the previous page sentence.
        // We find the first period, exclamation mark or question mark.
        val punctuationIndices = listOf(
            trimmed.indexOf('.'),
            trimmed.indexOf('!'),
            trimmed.indexOf('?')
        ).filter { it >= 0 }

        if (punctuationIndices.isNotEmpty()) {
            val firstEnd = punctuationIndices.minOrNull() ?: -1
            if (firstEnd >= 0 && firstEnd + 1 < trimmed.length) {
                val candidate = trimmed.substring(firstEnd + 1).trim()
                // Drop initial quotes or spaces if any
                return candidate.trimStart(' ', '\n', '\r', '"', '»')
            }
        }
        return trimmed
    }
}
