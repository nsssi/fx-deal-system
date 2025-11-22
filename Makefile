# ===============================
# FX DEAL SYSTEM – MAKEFILE
# ===============================

# Commandes Maven
MVN = mvn

# ===============================
# BUILD & RUN
# ===============================

# Build le projet + jar
build:
	$(MVN) clean package -DskipTests

# Démarrer les containers Docker
up:
	docker compose up --build

# Arrêter les containers et supprimer volumes
down:
	docker compose down -v

# ===============================
# TESTS
# ===============================

# Lancer tous les tests Maven
test:
	$(MVN) clean test

# Générer rapport de couverture JaCoCo
coverage:
	$(MVN) jacoco:report

# ===============================
# MAINTENANCE
# ===============================

# Clean complet + rebuild + redémarrage docker
rebuild:
	$(MVN) clean package -DskipTests
	docker compose down -v
	docker compose up --build

# Supprimer target/
clean:
	$(MVN) clean

# ===============================
# K6 TESTS
# ===============================

# Test K6 complet (toutes les API)
k6-full:
	k6 run k6_full_api_test.js

# Test K6 valid cases uniquement
k6-valid:
	k6 run k6_valid_test.js

# ===============================
# INFO
# ===============================
help:
	@echo "========== FX DEAL SYSTEM – COMMANDES DISPONIBLES =========="
	@echo " make build     → build Maven (skip tests)"
	@echo " make up        → start docker compose"
	@echo " make down      → stop docker + volumes"
	@echo " make test      → run all Maven tests"
	@echo " make coverage  → Jacoco report"
	@echo " make rebuild   → clean + build + restart docker"
	@echo " make k6-full   → run all K6 API tests"
	@echo " make k6-valid  → run only valid K6 tests"
	@echo " make clean     → mvn clean"
	@echo "============================================================"
