VERSION := 0.0.$$(git rev-list --count HEAD)

fmt:
	@./gradlew spotlessApply -Pversion=$(VERSION)

check:
	@./gradlew check -Pversion=$(VERSION) --rerun-tasks

build:
	@./gradlew build -Pversion=$(VERSION)

localpub:
	@./gradlew publishToMavenLocal -Pversion=$(VERSION)