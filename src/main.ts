import { NestFactory } from '@nestjs/core';
import { Logger, ValidationPipe } from '@nestjs/common';
import helmet from 'helmet';
import * as compressionPkg from 'compression';
const compression = compressionPkg.default || compressionPkg;
import { AppModule } from './app.module';

import { SwaggerModule, DocumentBuilder } from '@nestjs/swagger';
import { ConfigService } from '@nestjs/config';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const logger = new Logger('Bootstrap');
  const config = app.get(ConfigService);

  // Security headers
  app.use(helmet());

  // Compress responses
  app.use(compression());

  // Enable CORS (do NOT use "*" with credentials)
  const corsOrigin = config.get<string>('CORS_ORIGIN') ?? '*';
  const corsOrigins =
    corsOrigin === '*'
      ? '*'
      : corsOrigin
          .split(',')
          .map((o) => o.trim())
          .filter(Boolean);

  app.enableCors({
    origin: corsOrigins,
    methods: 'GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS',
    credentials: corsOrigin !== '*',
  });

  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      transform: true,
      forbidNonWhitelisted: true,
    }),
  );

  // Swagger (disable by default in production)
  const enableSwagger = (config.get<string>('ENABLE_SWAGGER') ?? 'false') === 'true';
  if (enableSwagger) {
    const swaggerConfig = new DocumentBuilder()
      .setTitle('BarberX API')
      .setDescription('Barber SaaS Backend APIs')
      .setVersion('1.0')
      .addBearerAuth()
      .build();

    const document = SwaggerModule.createDocument(app, swaggerConfig);
    SwaggerModule.setup('api', app, document);
  }

  const port = config.get<number>('PORT') ?? 3000;
  await app.listen(port);

  logger.log(`Server running on port: ${port}`);
  if (enableSwagger) {
    logger.log(`Swagger running on: http://localhost:${port}/api`);
  }
}
bootstrap();